const API_BASE_URL = '/api';
let allProducts = [];

function showNotification(message, type = 'success', duration = 3000) {
    let notifications = document.getElementById('notifications');
    if (!notifications) {
        notifications = document.createElement('div');
        notifications.className = 'notifications';
        notifications.id = 'notifications';
        document.body.appendChild(notifications);
    }

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
    if (!price) return '0 ₽';
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 0
    }).format(price);
}

async function login(username, password) {
    try {
        console.log('Пытаюсь войти с:', username);
        const response = await fetch(`${API_BASE_URL}/admin/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || `Ошибка входа: ${response.status}`);
        }

        const adminData = await response.json();
        console.log('Вход успешен:', adminData);

        localStorage.setItem('adminData', JSON.stringify(adminData));
        localStorage.setItem('isAdmin', 'true');

        showNotification('Вход выполнен успешно!', 'success');
        return adminData;
    } catch (error) {
        console.error('Ошибка авторизации:', error);
        showNotification('Неверные учетные данные или сервер не отвечает', 'error');
        throw error;
    }
}

function isAuthenticated() {
    return localStorage.getItem('isAdmin') === 'true';
}

function logout() {
    localStorage.removeItem('adminData');
    localStorage.removeItem('isAdmin');
    localStorage.removeItem('adminToken');
    showNotification('Вы вышли из системы', 'info');
}

async function loadProducts() {
    try {
        console.log('Запрашиваю товары с:', `${API_BASE_URL}/products`);
        const response = await fetch(`${API_BASE_URL}/products`);

        console.log('Статус ответа:', response.status);
        console.log('Заголовки ответа:', response.headers);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Текст ошибки:', errorText);
            throw new Error(`HTTP error! status: ${response.status}, text: ${errorText}`);
        }

        const data = await response.json();
        console.log('Получено товаров:', data.length);
        console.log('Первый товар:', data[0]);
        return data;
    } catch (error) {
        console.error('Полная ошибка загрузки товаров:', error);
        showNotification('Не удалось загрузить товары: ' + error.message, 'error');
        return [];
    }
}

function displayProducts(products) {
  const grid = document.getElementById('productsGrid');
  if (!grid) {
    console.error('Элемент productsGrid не найден!');
    return;
  }

  if (!products || products.length === 0) {
    grid.innerHTML = `
      <div class="empty-state" style="text-align: center; padding: 3rem; color: #666;">
        <i class="fas fa-box-open" style="font-size: 3rem; margin-bottom: 1rem;"></i>
        <p>Товары не найдены</p>
      </div>
    `;
    return;
  }

  const safeText = (v) => String(v ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');

  const getImageUrl = (product) => {
    if (!product?.imageUrl) return '';
    const url = String(product.imageUrl).trim();
    if (!url) return '';
    if (url.startsWith('http')) return url;
    return `/uploads/${url.replace(/^\/+/, '')}`;
  };

  grid.innerHTML = products
    .filter(p => !p.isDeleted)
    .map(product => {
      const price = product.salePrice || product.price;
      const hasSale = product.isOnSale && product.salePrice;

      const imageUrl = getImageUrl(product);
      const name = safeText(product.name);
      const category = safeText(product.category || 'Без категории');
      const description = safeText(product.description || 'Описание отсутствует');

      const imageBlock = imageUrl
        ? `
          <div class="product-image">
            <img
              src="${imageUrl}"
              alt="${name}"
              loading="lazy"
              onerror="this.onerror=null; this.outerHTML='<div class=&quot;product-placeholder&quot;><i class=&quot;fas fa-drumstick-bite&quot;></i><span>Фото скоро будет</span></div>'"
            >
          </div>
        `
        : `
          <div class="product-image">
            <div class="product-placeholder">
              <i class="fas fa-drumstick-bite"></i>
              <span>Фото скоро будет</span>
            </div>
          </div>
        `;

      return `
        <div class="product-card" data-id="${product.id}" data-category="${category}">
          ${hasSale ? '<span class="product-badge">Скидка</span>' : ''}

          ${imageBlock}

          <div class="product-info">
            <span class="product-category">${category}</span>
            <h3 class="product-title">${name}</h3>
            <p class="product-description">${description}</p>

            <div class="product-price">
              <div>
                ${hasSale ? `<span class="old-price">${formatPrice(product.price)}</span>` : ''}
                <span class="price">${formatPrice(price)}</span>
              </div>
              <span class="product-weight">${product.weight ? `${product.weight} кг` : 'Вес не указан'}</span>
            </div>

            <div class="product-actions">
              <button class="view-details-btn" data-id="${product.id}">
                <i class="fas fa-eye"></i>
                <span>Подробнее</span>
              </button>
            </div>
          </div>
        </div>
      `;
    })
    .join('');

  addProductEventListenersOnce();
}



function addProductEventListenersOnce() {
  const grid = document.getElementById('productsGrid');
  if (!grid) return;

  if (grid.dataset.detailsBound === '1') return;
  grid.dataset.detailsBound = '1';

  grid.addEventListener('click', (e) => {
    const btn = e.target.closest('.view-details-btn');
    if (!btn) return;

    const productId = Number(btn.dataset.id);
    const product = allProducts.find(p => p.id === productId);
    if (product) showProductDetails(product);
  });
}

function showProductDetails(product) {
  const modal = document.createElement('div');
  modal.className = 'modal show';

  const safeText = (v) => String(v ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');

  const getImageUrl = (p) => {
    if (!p?.imageUrl) return '';
    const url = String(p.imageUrl).trim();
    if (!url) return '';
    if (url.startsWith('http')) return url;
    return `/uploads/${url.replace(/^\/+/, '')}`;
  };

  const name = safeText(product.name);
  const category = safeText(product.category || 'Без категории');
  const description = safeText(product.description || 'Описание отсутствует.');
  const imageUrl = getImageUrl(product);

  const imageBlock = imageUrl
    ? `<img class="details-image" src="${imageUrl}" alt="${name}" loading="lazy"
         onerror="this.onerror=null; this.outerHTML='<div class=&quot;details-placeholder&quot;><i class=&quot;fas fa-drumstick-bite&quot;></i><span>Фото скоро будет</span></div>'">`
    : `<div class="details-placeholder"><i class="fas fa-drumstick-bite"></i><span>Фото скоро будет</span></div>`;

  const price = product.salePrice || product.price;
  const hasSale = product.isOnSale && product.salePrice;

  modal.innerHTML = `
    <div class="modal-content product-details-modal">
      <button class="modal-close" onclick="this.closest('.modal').remove()">
        <i class="fas fa-times"></i>
      </button>

      <div class="modal-header">
        <h2>${name}</h2>
        <p>${category}</p>
      </div>

      <div class="product-details-body">
        <div class="product-details-grid">
          <div class="product-details-left">
            ${imageBlock}
          </div>

          <div class="product-details-right">
            <h3>Информация о товаре</h3>

            <div class="details-row">
              <span>Цена</span>
              <div class="details-price">
                ${formatPrice(price)}
                ${hasSale ? `<span class="details-old-price">${formatPrice(product.price)}</span>` : ''}
              </div>
            </div>

            <div class="details-row">
              <span>Вес</span>
              <strong>${product.weight ? `${safeText(product.weight)} кг` : 'Не указан'}</strong>
            </div>

            <div class="details-row">
              <span>На складе</span>
              <strong>${Number.isFinite(product.quantity) ? `${product.quantity} шт.` : '—'}</strong>
            </div>

            <div class="details-row">
              <span>Артикул</span>
              <strong>${safeText(product.article || '—')}</strong>
            </div>

            <div class="details-description">
              <h3>Описание</h3>
              <p>${description}</p>
            </div>

            <button class="btn btn-outline" style="width:100%;" onclick="this.closest('.modal').remove()">
              <i class="fas fa-times"></i>
              <span>Закрыть</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  `;

  document.body.appendChild(modal);

  modal.addEventListener('click', (e) => {
    if (e.target === modal) modal.remove();
  });
}




function setupCategoryFilters() {
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
            this.classList.add('active');

            const category = this.dataset.category;
            filterProductsByCategory(category);
        });
    });

    document.querySelectorAll('.footer-links a[data-category]').forEach(link => {
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

function filterProductsByCategory(category) {
    if (category === 'all') {
        displayProducts(allProducts);
    } else {
        const filtered = allProducts.filter(product =>
            product.category === category && !product.isDeleted
        );
        displayProducts(filtered);
    }
}

async function initProducts() {
    try {
        allProducts = await loadProducts();
        displayProducts(allProducts);
    } catch (error) {
        console.error('Error loading products:', error);
    }
}

function initAuth() {
    const loginBtn = document.getElementById('loginBtn');
    const loginModal = document.getElementById('loginModal');
    const closeLoginModal = document.getElementById('closeLoginModal');
    const loginForm = document.getElementById('loginForm');

    updateAuthButton();

    if (loginBtn) {
        loginBtn.addEventListener('click', () => {
            loginModal.classList.add('show');
        });
    }

    if (closeLoginModal) {
        closeLoginModal.addEventListener('click', () => {
            loginModal.classList.remove('show');
        });
    }

    loginModal.addEventListener('click', (e) => {
        if (e.target === loginModal) {
            loginModal.classList.remove('show');
        }
    });

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            try {
                await login(username, password);
                loginModal.classList.remove('show');

                updateAuthButton();

                setTimeout(() => {
                    window.open('admin.html', '_blank');
                }, 500);

            } catch (error) {
                console.error('Login failed:', error);
            }
        });
    }
}

function updateAuthButton() {
    const authBtn = document.getElementById('loginBtn');
    if (!authBtn) return;

    if (isAuthenticated()) {
        const admin = JSON.parse(localStorage.getItem('adminData') || '{}');
        authBtn.innerHTML = `
            <i class="fas fa-user-check"></i>
            <span>${admin?.username || 'Админ'}</span>
        `;
        authBtn.onclick = () => {
            window.open('admin.html', '_blank');
        };
    } else {
        authBtn.innerHTML = `
            <i class="fas fa-user"></i>
            <span>Войти для администратора</span>
        `;
        authBtn.onclick = () => {
            document.getElementById('loginModal').classList.add('show');
        };
    }
}

function initNavigation() {
    const header = document.querySelector('.header');
    const navLinks = document.querySelectorAll('.nav-link');

    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }

        const sections = document.querySelectorAll('section[id]');
        let current = '';

        sections.forEach(section => {
            const sectionTop = section.offsetTop;
            const sectionHeight = section.clientHeight;

            if (scrollY >= sectionTop - 100) {
                current = section.getAttribute('id');
            }
        });

        navLinks.forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('href') === `#${current}`) {
                link.classList.add('active');
            }
        });
    });
}

function initSmoothScrolling() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();

            const targetId = this.getAttribute('href');
            if (targetId === '#') return;

            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                window.scrollTo({
                    top: targetElement.offsetTop - 80,
                    behavior: 'smooth'
                });

                const navMenu = document.querySelector('.nav-menu');
                if (navMenu.classList.contains('show')) {
                    navMenu.classList.remove('show');
                    document.getElementById('menuToggle').innerHTML = '<i class="fas fa-bars"></i>';
                }
            }
        });
    });
}

function initMobileMenu() {
    const menuToggle = document.getElementById('menuToggle');
    const navMenu = document.querySelector('.nav-menu');

    if (menuToggle && navMenu) {
        menuToggle.addEventListener('click', () => {
            navMenu.classList.toggle('show');
            menuToggle.innerHTML = navMenu.classList.contains('show') ?
                '<i class="fas fa-times"></i>' :
                '<i class="fas fa-bars"></i>';
        });

        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', () => {
                navMenu.classList.remove('show');
                menuToggle.innerHTML = '<i class="fas fa-bars"></i>';
            });
        });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    console.log('Инициализация приложения...');

    initNavigation();
    initAuth();
    initSmoothScrolling();
    initMobileMenu();

    if (document.getElementById('productsGrid')) {
        initProducts();
        setupCategoryFilters();
    }

    console.log('Инициализация завершена');
});

window.showProductDetails = showProductDetails;