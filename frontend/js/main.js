import { initProducts } from './products.js';
import { initAuth, isAuthenticated } from './auth.js';
import { cart } from './api.js';
import { showNotification } from './utils.js';

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initAuth();
    initCart();
    initSmoothScrolling();
    initMobileMenu();
    initNewsletter();
    initAnimations();
    
    // Инициализация продуктов только на главной странице
    if (document.getElementById('productsGrid')) {
        initProducts();
    }
});

// Инициализация навигации
function initNavigation() {
    const header = document.querySelector('.header');
    const navLinks = document.querySelectorAll('.nav-link');
    
    // Эффект при прокрутке
    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
        
        // Подсветка активного раздела
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

// Инициализация корзины
function initCart() {
    const cartBtn = document.getElementById('cartBtn');
    const cartModal = document.getElementById('cartModal');
    const closeCartModal = document.getElementById('closeCartModal');
    const checkoutBtn = document.getElementById('checkoutBtn');
    const cartContent = document.getElementById('cartContent');
    
    // Открытие корзины
    if (cartBtn) {
        cartBtn.addEventListener('click', () => {
            updateCartDisplay();
            cartModal.classList.add('show');
        });
    }
    
    // Закрытие корзины
    if (closeCartModal) {
        closeCartModal.addEventListener('click', () => {
            cartModal.classList.remove('show');
        });
    }
    
    // Закрытие при клике вне окна
    cartModal.addEventListener('click', (e) => {
        if (e.target === cartModal) {
            cartModal.classList.remove('show');
        }
    });
    
    // Оформление заказа
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', () => {
            if (isAuthenticated()) {
                showNotification('Заказ оформлен! Спасибо за покупку!', 'success');
                cart.clear();
                updateCartDisplay();
                cartModal.classList.remove('show');
            } else {
                showNotification('Для оформления заказа необходимо войти в систему', 'info');
                cartModal.classList.remove('show');
                setTimeout(() => {
                    document.getElementById('loginModal').classList.add('show');
                }, 500);
            }
        });
    }
    
    // Обновление отображения корзины
    window.updateCartDisplay = function() {
        const items = cart.getItems();
        const total = cart.getTotal();
        
        if (items.length === 0) {
            cartContent.innerHTML = `
                <div class="cart-empty">
                    <i class="fas fa-shopping-cart"></i>
                    <p>Корзина пуста</p>
                </div>
            `;
            checkoutBtn.disabled = true;
        } else {
            cartContent.innerHTML = items.map(item => `
                <div class="cart-item">
                    <div class="cart-item-image">
                        ${item.imageUrl ? 
                            `<img src="${item.imageUrl}" alt="${item.name}" style="width: 100%; height: 100%; object-fit: cover;">` :
                            `<div style="display: flex; align-items: center; justify-content: center; height: 100%;">
                                <i class="fas fa-hamburger" style="font-size: 1.5rem; color: var(--primary-red);"></i>
                            </div>`
                        }
                    </div>
                    
                    <div class="cart-item-info">
                        <div class="cart-item-title">${item.name}</div>
                        <div class="cart-item-price">${item.salePrice || item.price} ₽ × ${item.quantity}</div>
                    </div>
                    
                    <div class="cart-item-actions">
                        <button class="quantity-btn minus" data-id="${item.id}">
                            <i class="fas fa-minus"></i>
                        </button>
                        <input type="number" class="quantity-input" value="${item.quantity}" min="1" data-id="${item.id}">
                        <button class="quantity-btn plus" data-id="${item.id}">
                            <i class="fas fa-plus"></i>
                        </button>
                        <button class="remove-item-btn" data-id="${item.id}">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            `).join('');
            
            // Добавляем обработчики для кнопок корзины
            cartContent.querySelectorAll('.quantity-btn.plus').forEach(btn => {
                btn.addEventListener('click', () => {
                    const productId = btn.dataset.id;
                    const item = items.find(i => i.id == productId);
                    if (item) {
                        cart.updateQuantity(productId, item.quantity + 1);
                        updateCartDisplay();
                    }
                });
            });
            
            cartContent.querySelectorAll('.quantity-btn.minus').forEach(btn => {
                btn.addEventListener('click', () => {
                    const productId = btn.dataset.id;
                    const item = items.find(i => i.id == productId);
                    if (item) {
                        cart.updateQuantity(productId, item.quantity - 1);
                        updateCartDisplay();
                    }
                });
            });
            
            cartContent.querySelectorAll('.quantity-input').forEach(input => {
                input.addEventListener('change', (e) => {
                    const productId = input.dataset.id;
                    const quantity = parseInt(e.target.value);
                    if (!isNaN(quantity) && quantity > 0) {
                        cart.updateQuantity(productId, quantity);
                        updateCartDisplay();
                    }
                });
            });
            
            cartContent.querySelectorAll('.remove-item-btn').forEach(btn => {
                btn.addEventListener('click', () => {
                    const productId = btn.dataset.id;
                    cart.removeItem(productId);
                    updateCartDisplay();
                });
            });
            
            checkoutBtn.disabled = false;
        }
        
        // Обновление общей суммы
        const totalPriceElement = document.querySelector('.total-price');
        if (totalPriceElement) {
            totalPriceElement.textContent = `${total.toLocaleString('ru-RU')} ₽`;
        }
    };
}

// Плавная прокрутка
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
                
                // Закрытие мобильного меню
                const navMenu = document.querySelector('.nav-menu');
                if (navMenu.classList.contains('show')) {
                    navMenu.classList.remove('show');
                }
            }
        });
    });
}

// Мобильное меню
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
        
        // Закрытие меню при клике на ссылку
        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', () => {
                navMenu.classList.remove('show');
                menuToggle.innerHTML = '<i class="fas fa-bars"></i>';
            });
        });
    }
}

// Подписка на рассылку
function initNewsletter() {
    const newsletterForm = document.querySelector('.newsletter-form');
    const newsletterInput = document.querySelector('.newsletter-input');
    const newsletterBtn = document.querySelector('.newsletter-btn');
    
    if (newsletterForm) {
        newsletterForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const email = newsletterInput.value.trim();
            
            if (email && validateEmail(email)) {
                showNotification('Спасибо за подписку!', 'success');
                newsletterInput.value = '';
                
                // В реальном приложении здесь бы был запрос к API
                console.log('Subscribed email:', email);
            } else {
                showNotification('Пожалуйста, введите корректный email', 'error');
            }
        });
    }
    
    if (newsletterBtn) {
        newsletterBtn.addEventListener('click', () => {
            newsletterForm.dispatchEvent(new Event('submit'));
        });
    }
}

// Валидация email
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

// Анимации при скролле
function initAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-in');
            }
        });
    }, observerOptions);
    
    // Наблюдаем за элементами, которые должны анимироваться
    document.querySelectorAll('.feature-card, .product-card, .contact-card').forEach(el => {
        observer.observe(el);
    });
    
    // Добавляем CSS для анимации
    const style = document.createElement('style');
    style.textContent = `
        .feature-card,
        .product-card,
        .contact-card {
            opacity: 0;
            transform: translateY(20px);
            transition: opacity 0.5s ease, transform 0.5s ease;
        }
        
        .animate-in {
            opacity: 1;
            transform: translateY(0);
        }
    `;
    document.head.appendChild(style);
}

// Глобальные функции для работы с корзиной
window.clearCart = function() {
    cart.clear();
    if (typeof updateCartDisplay === 'function') {
        updateCartDisplay();
    }
};

window.getCartItems = function() {
    return cart.getItems();
};

window.getCartTotal = function() {
    return cart.getTotal();
};