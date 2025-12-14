import { adminLogin, adminRegister } from './api.js';
import { showNotification } from './utils.js';

let currentAdmin = null;

export function isAuthenticated() {
    return localStorage.getItem('adminToken') !== null;
}

export function getCurrentAdmin() {
    if (!currentAdmin) {
        const adminData = localStorage.getItem('adminData');
        if (adminData) {
            currentAdmin = JSON.parse(adminData);
        }
    }
    return currentAdmin;
}

export async function login(username, password) {
    try {
        const response = await adminLogin({ username, password });

        localStorage.setItem('adminToken', 'demo-token');
        localStorage.setItem('adminData', JSON.stringify(response));
        currentAdmin = response;
        
        showNotification('Вход выполнен успешно!', 'success');
        return response;
    } catch (error) {
        showNotification('Ошибка входа. Проверьте данные.', 'error');
        throw error;
    }
}

export async function register(adminData) {
    try {
        const response = await adminRegister(adminData);
        showNotification('Администратор создан успешно!', 'success');
        return response;
    } catch (error) {
        showNotification('Ошибка регистрации.', 'error');
        throw error;
    }
}

export function logout() {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminData');
    currentAdmin = null;
    showNotification('Вы вышли из системы', 'info');
}

export function initAuth() {
    const loginModal = document.getElementById('loginModal');
    const loginBtn = document.getElementById('loginBtn');
    const closeLoginModal = document.getElementById('closeLoginModal');
    const loginForm = document.getElementById('loginForm');
    const showRegisterLink = document.getElementById('showRegister');

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

                updateAuthUI();

                setTimeout(() => {
                    window.location.href = 'admin.html';
                }, 1000);
            } catch (error) {
                console.error('Login failed:', error);
            }
        });
    }

    if (showRegisterLink) {
        showRegisterLink.addEventListener('click', (e) => {
            e.preventDefault();
            showNotification('Регистрация доступна только через API', 'info');
        });
    }

    updateAuthUI();
}

function updateAuthUI() {
    const authBtn = document.getElementById('loginBtn');
    if (!authBtn) return;
    
    if (isAuthenticated()) {
        const admin = getCurrentAdmin();
        authBtn.innerHTML = `
            <i class="fas fa-user-check"></i>
            <span>${admin?.username || 'Админ'}</span>
        `;
        authBtn.onclick = () => {
            window.location.href = 'admin.html';
        };
    } else {
        authBtn.innerHTML = `
            <i class="fas fa-user"></i>
            <span>Войти</span>
        `;
        authBtn.onclick = () => {
            document.getElementById('loginModal').classList.add('show');
        };
    }
}