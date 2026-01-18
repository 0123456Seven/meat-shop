// Конфигурация
const API_BASE_URL = '/api';

// Глобальные переменные
let allProducts = [];
let currentProductId = null;

// Утилиты
function showNotification(message, type = 'success', duration = 3000) {
    const notifications = document.getElementById('notifications');
    if (!notifications) return;
    
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
        <span>${message}</span>
    `;
    
    notifications.appendChild(notification);
    
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, duration);
}

function formatPrice(price) {
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 0
    }).format(price);
}

// API функции
async function apiRequest(endpoint, method = 'GET', data = null) {
    const url = `${API_BASE_URL}${endpoint}`;
    console.log(`API запрос: ${method} ${url}`);
    
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    };
    
    if (data && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(data);
    }
    
    try {
        const response = await fetch(url, options);
        
        console.log(`API ответ: ${response.status} ${response.statusText}`);
        
        if (response.status === 204) {
            return null;
        }
        
        if (!response.ok) {
            let errorText = await response.text();
            console.error('API ошибка текст:', errorText);
            try {
                const errorData = JSON.parse(errorText);
                throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
            } catch {
                throw new Error(errorText || `HTTP error! status: ${response.status}`);
            }
        }
        
        const data = await response.json();
        console.log('API успешно:', data);
        return data;
    } catch (error) {
        console.error('API Error:', error);
        showNotification(`Ошибка: ${error.message}`, 'error');
        throw error;
    }
}

async function loadProducts() {
    try {
        console.log('Загружаю товары...');
        return await apiRequest('/products');
    } catch (error) {
        console.error('Failed to load products:', error);
        showNotification('Ошибка загрузки товаров', 'error');
        return [];
    }
}

async function loadProductById(id) {
    try {
        console.log(`Загружаю товар с ID: ${id}`);
        return await apiRequest(`/products/${id}`);
    } catch (error) {
        console.error('Failed to load product:', error);
        throw error;
    }
}

async function saveProduct(productData) {
  const fileInput = document.getElementById('productImageFile');
  const file = fileInput?.files?.[0];

  let saved;
  if (currentProductId) {
    saved = await apiRequest(`/products/${currentProductId}`, 'PUT', productData);
  } else {
    saved = await apiRequest('/products', 'POST', productData);
    currentProductId = saved?.id; // важно
  }

  if (file && currentProductId) {
    showNotification('Загрузка изображения...', 'info');

    const formData = new FormData();
    formData.append('file', file);

    const uploadResponse = await fetch(`${API_BASE_URL}/products/${currentProductId}/upload-image`, {
      method: 'POST',
      body: formData
    });

    if (!uploadResponse.ok) {
      const errText = await uploadResponse.text();
      console.error('UPLOAD ERROR:', uploadResponse.status, errText);
      throw new Error(`Ошибка загрузки изображения: ${uploadResponse.status}`);
    }

    const uploadResult = await uploadResponse.json();

    const imageUrl = uploadResult.filePath;
    if (imageUrl) {
      await apiRequest(`/products/${currentProductId}`, 'PUT', { ...saved, imageUrl });
    }

    showNotification('Изображение загружено', 'success');
  }

  return saved;
}


async function deleteProductById(id) {
    try {
        await apiRequest(`/products/${id}`, 'DELETE');
        return true;
    } catch (error) {
        console.error('Failed to delete product:', error);
        throw error;
    }
}

function renderProductsTable() {
    const tableBody = document.getElementById('productsTableBody');
    if (!tableBody) return;
    
    const searchTerm = document.getElementById('productSearch').value.toLowerCase();
    const categoryFilter = document.getElementById('categoryFilter').value;

    let filteredProducts = allProducts.filter(product => !product.isDeleted);
    
    if (searchTerm) {
        filteredProducts = filteredProducts.filter(product => 
            product.name.toLowerCase().includes(searchTerm) ||
            product.article.toLowerCase().includes(searchTerm) ||
            (product.description && product.description.toLowerCase().includes(searchTerm))
        );
    }
    
    if (categoryFilter !== 'all') {
        filteredProducts = filteredProducts.filter(product => 
            product.category === categoryFilter
        );
    }

    document.getElementById('totalProducts').textContent = `${filteredProducts.length} товаров`;
    
    if (filteredProducts.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="8" style="text-align: center; padding: 3rem; color: var(--gray-500);">
                    <i class="fas fa-box-open" style="font-size: 2rem; margin-bottom: 1rem; display: block;"></i>
                    <p>Товары не найдены</p>
                </td>
            </tr>
        `;
        return;
    }
    
    tableBody.innerHTML = filteredProducts.map(product => {
        let imageSrc = '';
        if (product.imageUrl) {
            if (product.imageUrl.startsWith('http')) {
                imageSrc = product.imageUrl;
            } else {
                const fileName = product.imageUrl.split('/').pop();
                imageSrc = `/uploads/${fileName}`;
            }
        }
        
        return `
        <tr data-id="${product.id}">
            <td class="product-id">${product.id}</td>
            <td class="product-article">${product.article}</td>
            <td>
                <div style="display: flex; align-items: center; gap: 1rem;">
                    ${imageSrc ? 
                        `<img src="${imageSrc}" 
                              style="width: 50px; height: 50px; border-radius: var(--radius-md); object-fit: cover; border: 1px solid var(--gray-200);"
                              onerror="this.style.display='none'">` : 
                        '<div style="width: 50px; height: 50px; background: var(--gray-100); border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; color: var(--gray-400);"><i class="fas fa-image"></i></div>'
                    }
                    <div>
                        <div class="product-name">${product.name}</div>
                        ${product.description ? `<div style="font-size: 0.875rem; color: var(--gray-600); margin-top: 0.25rem;">${product.description.substring(0, 50)}...</div>` : ''}
                    </div>
                </div>
            </td>
            <td><span class="product-category">${product.category || 'Без категории'}</span></td>
            <td>
                ${product.isOnSale && product.salePrice ? 
                    `<span class="product-price">${formatPrice(product.salePrice)}</span>
                     <span class="product-old-price">${formatPrice(product.price)}</span>` :
                    `<span class="product-price">${formatPrice(product.price)}</span>`
                }
            </td>
            <td>
                <span class="quantity-badge ${product.quantity > 10 ? 'in-stock' : product.quantity > 0 ? 'low-stock' : 'out-of-stock'}">
                    ${product.quantity} шт.
                </span>
            </td>
            <td>
                <span class="status-badge ${product.quantity > 0 ? 'active' : 'inactive'}">
                    ${product.quantity > 0 ? 'В наличии' : 'Нет в наличии'}
                </span>
            </td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn edit-btn" onclick="openEditModal(${product.id})" title="Редактировать">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="action-btn delete-btn" onclick="deleteProduct(${product.id})" title="Удалить">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
        `;
    }).join('');
}

async function refreshProducts() {
    try {
        allProducts = await loadProducts();
        renderProductsTable();
        showNotification('Товары обновлены', 'success');
    } catch (error) {
        showNotification('Ошибка при загрузке товаров', 'error');
    }
}

async function openEditModal(productId) {
    try {
        currentProductId = productId;
        console.log(`Открываю редактирование товара ID: ${productId}`);
        
        const product = await loadProductById(productId);
        
        console.log('Получен товар для редактирования:', product);
        
        document.getElementById('productModalTitle').textContent = 'Редактировать товар';
        
        // Заполняем форму
        document.getElementById('productName').value = product.name || '';
        document.getElementById('productArticle').value = product.article || '';
        document.getElementById('productCategory').value = product.category || '';
        document.getElementById('productPrice').value = product.price || '';
        document.getElementById('productQuantity').value = product.quantity || 0;
        document.getElementById('productWeight').value = product.weight || '';
        document.getElementById('productDescription').value = product.description || '';
        
        // КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ: используем productImageUrl, а не productImage
        document.getElementById('productImageUrl').value = product.imageUrl || '';
        
        const productOnSaleCheckbox = document.getElementById('productOnSale');
        const salePriceContainer = document.getElementById('salePriceContainer');
        
        if (product.isOnSale) {
            productOnSaleCheckbox.checked = true;
            salePriceContainer.style.display = 'block';
            document.getElementById('productSalePrice').value = product.salePrice || '';
        } else {
            productOnSaleCheckbox.checked = false;
            salePriceContainer.style.display = 'none';
            document.getElementById('productSalePrice').value = '';
        }
        
        // Поле артикула только для чтения при редактировании
        document.getElementById('productArticle').readOnly = true;
        
        document.getElementById('productModal').classList.add('show');
    } catch (error) {
        console.error('Ошибка при загрузке товара:', error);
        showNotification(`Ошибка при загрузке товара: ${error.message}`, 'error');
    }
}

function openAddModal() {
    currentProductId = null;
    document.getElementById('productModalTitle').textContent = 'Добавить товар';

    document.getElementById('productForm').reset();
    document.getElementById('salePriceContainer').style.display = 'none';
    document.getElementById('productArticle').readOnly = false;
    
    document.getElementById('productModal').classList.add('show');
}

async function deleteProduct(productId) {
    if (!confirm('Вы уверены, что хотите удалить этот товар?')) return;
    
    try {
        await deleteProductById(productId);
        showNotification('Товар удален', 'success');
        await refreshProducts();
    } catch (error) {
        showNotification('Ошибка при удалении товара', 'error');
    }
}

document.addEventListener('DOMContentLoaded', function() {
    console.log('Админка инициализируется...');

    refreshProducts();

    const addProductBtn = document.getElementById('addProductBtn');
    if (addProductBtn) {
        addProductBtn.addEventListener('click', openAddModal);
    }

    const closeProductModal = document.getElementById('closeProductModal');
    if (closeProductModal) {
        closeProductModal.addEventListener('click', () => {
            document.getElementById('productModal').classList.remove('show');
        });
    }
    
    document.getElementById('cancelProductBtn')?.addEventListener('click', () => {
        document.getElementById('productModal').classList.remove('show');
    });
    
    document.getElementById('productModal')?.addEventListener('click', (e) => {
        if (e.target === document.getElementById('productModal')) {
            document.getElementById('productModal').classList.remove('show');
        }
    });

    document.getElementById('productOnSale')?.addEventListener('change', function() {
        const salePriceContainer = document.getElementById('salePriceContainer');
        if (this.checked) {
            salePriceContainer.style.display = 'block';
        } else {
            salePriceContainer.style.display = 'none';
            document.getElementById('productSalePrice').value = '';
        }
    });

    document.getElementById('productForm')?.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        console.log('Отправка формы...');
        
        const productData = {
            name: document.getElementById('productName').value,
            article: document.getElementById('productArticle').value,
            category: document.getElementById('productCategory').value || null,
            price: parseFloat(document.getElementById('productPrice').value),
            quantity: parseInt(document.getElementById('productQuantity').value),
            weight: document.getElementById('productWeight').value ? parseFloat(document.getElementById('productWeight').value) : null,
            description: document.getElementById('productDescription').value || null,
            imageUrl: document.getElementById('productImageUrl').value || null,
            isOnSale: document.getElementById('productOnSale').checked,
            salePrice: document.getElementById('productSalePrice').value ? parseFloat(document.getElementById('productSalePrice').value) : null
        };
        
        console.log('Данные для сохранения:', productData);
        
        try {
            await saveProduct(productData);
            showNotification(currentProductId ? 'Товар обновлен' : 'Товар добавлен', 'success');
            document.getElementById('productModal').classList.remove('show');
            await refreshProducts();

            document.getElementById('productImageFile').value = '';
        } catch (error) {
            console.error('Error saving product:', error);
            showNotification(`Ошибка сохранения: ${error.message}`, 'error');
        }
    });

    document.getElementById('productSearch')?.addEventListener('input', renderProductsTable);

    document.getElementById('categoryFilter')?.addEventListener('change', renderProductsTable);

    document.getElementById('refreshBtn')?.addEventListener('click', refreshProducts);

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            localStorage.removeItem('adminData');
            localStorage.removeItem('isAdmin');
            localStorage.removeItem('adminToken');

            alert('Вы вышли из системы');

            window.location.href = 'index.html';
        });
    }

    document.getElementById('sidebarToggle')?.addEventListener('click', function() {
        document.querySelector('.admin-sidebar').classList.toggle('show');
    });
    
    console.log('Админка инициализирована');
});

window.openEditModal = openEditModal;
window.deleteProduct = deleteProduct;
