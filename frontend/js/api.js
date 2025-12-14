import { showNotification } from './utils.js';

const API_BASE_URL = '/api';

async function makeRequest(endpoint, method = 'GET', data = null) {
    const url = `${API_BASE_URL}${endpoint}`;
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
        
        if (response.status === 204) {
            return null;
        }
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || `HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('API Error:', error);
        showNotification(`Ошибка: ${error.message}`, 'error');
        throw error;
    }
}

// Товары
export async function getProducts() {
    return await makeRequest('/products');
}

export async function getProductById(id) {
    return await makeRequest(`/products/${id}`);
}

export async function getProductsByCategory(category) {
    return await makeRequest(`/products/category/${category}`);
}

export async function createProduct(productData) {
    return await makeRequest('/products', 'POST', productData);
}

export async function updateProduct(id, productData) {
    return await makeRequest(`/products/${id}`, 'PUT', productData);
}

export async function deleteProduct(id) {
    return await makeRequest(`/products/${id}`, 'DELETE');
}

// Администраторы
export async function adminLogin(loginData) {
    return await makeRequest('/admin/login', 'POST', loginData);
}

export async function adminRegister(adminData) {
    return await makeRequest('/admin/register', 'POST', adminData);
}

export async function getAdmins() {
    return await makeRequest('/admin');
}

export async function getAdminById(id) {
    return await makeRequest(`/admin/${id}`);
}

// Корзина (локальное хранилище)
export const cart = {
    getItems() {
        const cartData = localStorage.getItem('cart');
        return cartData ? JSON.parse(cartData) : [];
    },
    
    addItem(product, quantity = 1) {
        const items = this.getItems();
        const existingItem = items.find(item => item.id === product.id);
        
        if (existingItem) {
            existingItem.quantity += quantity;
        } else {
            items.push({
                ...product,
                quantity: quantity
            });
        }
        
        this.saveItems(items);
        this.updateCartCount();
        showNotification('Товар добавлен в корзину!');
    },
    
    removeItem(productId) {
        let items = this.getItems();
        items = items.filter(item => item.id !== productId);
        this.saveItems(items);
        this.updateCartCount();
        showNotification('Товар удален из корзины', 'info');
    },
    
    updateQuantity(productId, quantity) {
        const items = this.getItems();
        const item = items.find(item => item.id === productId);
        
        if (item) {
            if (quantity <= 0) {
                this.removeItem(productId);
            } else {
                item.quantity = quantity;
                this.saveItems(items);
                this.updateCartCount();
            }
        }
    },
    
    clear() {
        localStorage.removeItem('cart');
        this.updateCartCount();
        showNotification('Корзина очищена', 'info');
    },
    
    getTotal() {
        const items = this.getItems();
        return items.reduce((total, item) => {
            const price = item.salePrice || item.price;
            return total + (price * item.quantity);
        }, 0);
    },
    
    getItemCount() {
        const items = this.getItems();
        return items.reduce((count, item) => count + item.quantity, 0);
    },
    
    saveItems(items) {
        localStorage.setItem('cart', JSON.stringify(items));
    },
    
    updateCartCount() {
        const count = this.getItemCount();
        const cartCountElements = document.querySelectorAll('.cart-count');
        cartCountElements.forEach(el => {
            el.textContent = count;
        });
    }
};