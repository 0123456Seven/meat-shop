import { getProducts, getProductsByCategory, cart } from './api.js';
import { formatPrice, showNotification } from './utils.js';

let allProducts = [];
let currentCategory = 'all';
let productsPerPage = 12;
let currentPage = 1;

export async function initProducts() {
    try {
        const loadingElement = document.getElementById('productsGrid');
        if (loadingElement) {
            loadingElement.innerHTML = `
                <div class="loading-spinner">
                    <div class="spinner"></div>
                    <p>Загрузка товаров...</p>
                </div>
            `;
        }
        
        allProducts = await getProducts();
        displayProducts(allProducts);
        initCategoryFilter();
        initProductEvents();

        cart.updateCartCount();
    } catch (error) {
        console.error('Error loading products:', error);
        const grid = document.getElementById('productsGrid');
        if (grid) {
            grid.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-triangle"></i>
                    <p>Не удалось загрузить товары. Пожалуйста, попробуйте позже.</p>
                </div>
            `;
        }
    }
}

export function displayProducts(products) {
    const grid = document.getElementById('productsGrid');
    if (!grid) return;
    
    if (!products || products.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-box-open"></i>
                <p>Товары не найдены</p>
            </div>
        `;
        return;
    }

    const activeProducts = products.filter(product => !product.isDeleted);
    
    grid.innerHTML = activeProducts.map(product => createProductCard(product)).join('');
}

function createProductCard(product) {
    const price = product.salePrice || product.price;
    const hasSale = product.isOnSale && product.salePrice;
    
    return `
        <div class="product-card" data-id="${product.id}" data-category="${product.category || 'Без категории'}">
            ${hasSale ? '<span class="product-badge">Скидка</span>' : ''}
            
            <div class="product-image">
                ${product.imageUrl ? 
                    `<img src="${product.imageUrl}" alt="${product.name}" loading="lazy">` : 
                    `<div style="display: flex; align-items: center; justify-content: center; height: 100%;">
                        <i class="fas fa-hamburger" style="font-size: 3rem; color: var(--primary-red);"></i>
                    </div>`
                }
            </div>
            
            <div class="product-info">
                <span class="product-category">${product.category || 'Без категории'}</span>
                <h3 class="product-title">${product.name}</h3>
                <p class="product-description">${product.description || 'Описание отсутствует'}</p>
                
                <div class="product-price">
                    <div>
                        ${hasSale ? 
                            `<span class="old-price">${formatPrice(product.price)}</span>` : 
                            ''
                        }
                        <span class="price">${formatPrice(price)}</span>
                    </div>
                    <span class="product-weight">${product.weight ? `${product.weight} кг` : 'Вес не указан'}</span>
                </div>
                
                <div class="product-actions">
                    <button class="add-to-cart-btn" data-id="${product.id}">
                        <i class="fas fa-cart-plus"></i>
                        <span>В корзину</span>
                    </button>
                    <button class="view-details-btn" data-id="${product.id}">
                        <i class="fas fa-eye"></i>
                    </button>
                </div>
            </div>
        </div>
    `;
}

function initCategoryFilter() {
    const filterButtons = document.querySelectorAll('.filter-btn');
    filterButtons.forEach(button => {
        button.addEventListener('click', () => {
            filterButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            
            currentCategory = button.dataset.category;
            filterProductsByCategory(currentCategory);
        });
    });

    const footerCategoryLinks = document.querySelectorAll('.footer-links a[data-category]');
    footerCategoryLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const category = link.dataset.category;

            const productsSection = document.getElementById('products');
            if (productsSection) {
                productsSection.scrollIntoView({ behavior: 'smooth' });
            }

            const targetButton = document.querySelector(`.filter-btn[data-category="${category}"]`);
            if (targetButton) {
                targetButton.click();
            }
        });
    });
}

async function filterProductsByCategory(category) {
    try {
        if (category === 'all') {
            displayProducts(allProducts);
        } else {
            const filteredProducts = await getProductsByCategory(category);
            displayProducts(filteredProducts);
        }
    } catch (error) {
        console.error('Error filtering products:', error);
        showNotification('Ошибка при фильтрации товаров', 'error');
    }
}

function initProductEvents() {
    const grid = document.getElementById('productsGrid');
    if (!grid) return;

    grid.addEventListener('click', (e) => {
        const addToCartBtn = e.target.closest('.add-to-cart-btn');
        if (addToCartBtn) {
            const productId = addToCartBtn.dataset.id;
            const product = allProducts.find(p => p.id == productId);
            if (product) {
                cart.addItem(product);
            }
        }

        const viewDetailsBtn = e.target.closest('.view-details-btn');
        if (viewDetailsBtn) {
            const productId = viewDetailsBtn.dataset.id;
            showProductDetails(productId);
        }
    });
}

function showProductDetails(productId) {
    const product = allProducts.find(p => p.id == productId);
    if (!product) return;
    
    const modal = document.createElement('div');
    modal.className = 'modal show';
    modal.innerHTML = `
        <div class="modal-content">
            <button class="modal-close">
                <i class="fas fa-times"></i>
            </button>
            
            <div class="modal-header">
                <h2>${product.name}</h2>
                <p>${product.category || 'Без категории'}</p>
            </div>
            
            <div style="padding: 2rem;">
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; margin-bottom: 2rem;">
                    <div style="background: var(--gray-100); border-radius: var(--radius-lg); padding: 2rem; display: flex; align-items: center; justify-content: center;">
                        ${product.imageUrl ? 
                            `<img src="${product.imageUrl}" alt="${product.name}" style="max-width: 100%; max-height: 200px;">` :
                            `<i class="fas fa-hamburger" style="font-size: 4rem; color: var(--primary-red);"></i>`
                        }
                    </div>
                    
                    <div>
                        <h3 style="margin-bottom: 1rem;">Информация о товаре</h3>
                        <div style="display: grid; gap: 1rem;">
                            <div>
                                <strong>Цена:</strong>
                                <div style="font-size: 1.5rem; font-weight: bold; color: var(--primary-red);">
                                    ${formatPrice(product.salePrice || product.price)}
                                    ${product.isOnSale && product.salePrice ? 
                                        `<span style="font-size: 1rem; color: var(--gray-500); text-decoration: line-through; margin-left: 0.5rem;">
                                            ${formatPrice(product.price)}
                                        </span>` : ''
                                    }
                                </div>
                            </div>
                            
                            <div>
                                <strong>Вес:</strong>
                                <div>${product.weight ? `${product.weight} кг` : 'Не указан'}</div>
                            </div>
                            
                            <div>
                                <strong>Количество на складе:</strong>
                                <div>${product.quantity} шт.</div>
                            </div>
                            
                            <div>
                                <strong>Артикул:</strong>
                                <div>${product.article}</div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div style="margin-bottom: 2rem;">
                    <h3 style="margin-bottom: 1rem;">Описание</h3>
                    <p style="color: var(--gray-700); line-height: 1.6;">
                        ${product.description || 'Описание отсутствует.'}
                    </p>
                </div>
                
                <button class="btn btn-primary" style="width: 100%;" id="addToCartModalBtn">
                    <i class="fas fa-cart-plus"></i>
                    <span>Добавить в корзину</span>
                </button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);

    const closeBtn = modal.querySelector('.modal-close');
    closeBtn.addEventListener('click', () => {
        modal.remove();
    });

    const addToCartBtn = modal.querySelector('#addToCartModalBtn');
    addToCartBtn.addEventListener('click', () => {
        cart.addItem(product);
        modal.remove();
    });

    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.remove();
        }
    });
}

function initCounters() {
    const counters = document.querySelectorAll('.stat-number');
    
    counters.forEach(counter => {
        const target = parseInt(counter.dataset.count);
        const duration = 2000;
        const increment = target / (duration / 16);
        let current = 0;
        
        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                counter.textContent = target;
                clearInterval(timer);
            } else {
                counter.textContent = Math.floor(current);
            }
        }, 16);
    });
}

document.addEventListener('DOMContentLoaded', () => {
    initProducts();
    initCounters();
});