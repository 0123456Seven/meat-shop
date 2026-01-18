// cart.js — логика страницы корзины
const CART_KEY = 'cart_v1';

function updateCartBadge() {
  const el = document.getElementById('cartCount');
  if (!el) return;

  const cart = getCart();
  const uniqueCount = Object.keys(cart.items || {}).length;

  el.textContent = String(uniqueCount);
  el.style.display = uniqueCount > 0 ? 'flex' : 'none';
}

function getCart() {
  try {
    const raw = localStorage.getItem(CART_KEY);
    return raw ? JSON.parse(raw) : { items: {} };
  } catch {
    return { items: {} };
  }
}

function saveCart(cart) {
  localStorage.setItem(CART_KEY, JSON.stringify(cart));
}

function formatPrice(price) {
  if (!price) return '0 ₽';
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 0
  }).format(price);
}

function calcTotal(cart) {
  return Object.values(cart.items).reduce((sum, it) => {
    const p = it.product?.price || 0;
    return sum + (p * (Number(it.qty) || 0));
  }, 0);
}

function renderCart() {
  const cart = getCart();
  const list = document.getElementById('cartList');
  const empty = document.getElementById('cartEmpty');
  const totalEl = document.getElementById('cartTotal');

  const items = Object.values(cart.items);

  if (!items.length) {
    empty.style.display = 'block';
    list.innerHTML = '';
    totalEl.textContent = formatPrice(0);
    return;
  }

  empty.style.display = 'none';

  list.innerHTML = items.map(it => {
    const p = it.product;
    const id = String(p.id);
    const qty = Number(it.qty) || 1;

    return `
      <div style="display:flex; gap:14px; padding:14px; border-radius:16px; background: rgba(0,0,0,0.03); margin-bottom:12px; align-items:center;">
        <div style="flex:1;">
          <div style="font-weight:800;">${p.name}</div>
          <div style="opacity:.75; margin-top:4px;">Цена: ${formatPrice(p.price)}</div>
        </div>

        <div style="display:flex; align-items:center; gap:8px;">
          <button class="btn btn-outline" data-action="dec" data-id="${id}">-</button>
          <input
            type="number"
            min="1"
            value="${qty}"
            data-action="qty"
            data-id="${id}"
            style="width:70px; padding:10px; border-radius:12px; border:1px solid rgba(0,0,0,0.12);"
          />
          <button class="btn btn-outline" data-action="inc" data-id="${id}">+</button>
        </div>

        <div style="width:160px; text-align:right; font-weight:800;">
          ${formatPrice(p.price * qty)}
        </div>

        <button class="btn btn-outline" data-action="remove" data-id="${id}" title="Удалить">
          <i class="fas fa-xmark"></i>
        </button>
      </div>
    `;
  }).join('');

  totalEl.textContent = formatPrice(calcTotal(cart));
  updateCartBadge();
}

function updateQty(productId, newQty) {
  const cart = getCart();
  const id = String(productId);
  if (!cart.items[id]) return;

  const qty = Math.max(1, Number(newQty) || 1);
  cart.items[id].qty = qty;

  saveCart(cart);
  renderCart();
}

function removeItem(productId) {
  const cart = getCart();
  const id = String(productId);
  delete cart.items[id];
  saveCart(cart);
  renderCart();
}

function clearCart() {
  saveCart({ items: {} });
  renderCart();
}

function showToast(text) {
  // минимальный “тост”, чтобы не плодить алерты
  const box = document.getElementById('notifications');
  if (!box) return alert(text);

  const el = document.createElement('div');
  el.textContent = text;
  el.style.cssText = `
    background: #111;
    color: #fff;
    padding: 12px 14px;
    border-radius: 12px;
    margin-top: 10px;
    max-width: 420px;
  `;
  box.appendChild(el);
  setTimeout(() => el.remove(), 3500);
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(email || '').trim());
}

function normalizePhone(phone) {
  // оставляем только цифры
  const digits = String(phone || '').replace(/\D/g, '');

  if (!digits) return '';

  // 11 цифр: 8XXXXXXXXXX или 7XXXXXXXXXX
  if (digits.length === 11 && digits.startsWith('8')) {
    return '+7' + digits.slice(1);
  }
  if (digits.length === 11 && digits.startsWith('7')) {
    return '+7' + digits.slice(1);
  }

  // 10 цифр: XXXXXXXXXX -> +7XXXXXXXXXX
  if (digits.length === 10) {
    return '+7' + digits;
  }

  // иначе вернем просто цифры (для отладки), но это будет считаться невалидным
  return digits;
}

function isValidPhone(phone) {
  // phone уже может быть нормализован (например +7...)
  const digits = String(phone || '').replace(/\D/g, '');
  // для РФ ожидаем 11 цифр (7 + 10 цифр)
  return digits.length === 11 && digits.startsWith('7');
}



async function createOrderOnBackend(payload) {
  const res = await fetch('/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
    body: JSON.stringify(payload)
  });

  // бэк у тебя отдаёт красивую ошибку JSON
  const data = await res.json().catch(() => null);

  if (!res.ok) {
    const msg = data?.message || `Ошибка HTTP: ${res.status}`;
    throw new Error(msg);
  }

  return data; // ожидаем { id: ... }
}


document.addEventListener('DOMContentLoaded', () => {
  renderCart();

    const modal = document.getElementById('checkoutModal');
    const cancelBtn = document.getElementById('cancelCheckoutBtn');
    const submitBtn = document.getElementById('submitOrderBtn');

    cancelBtn?.addEventListener('click', () => {
      modal.style.display = 'none';
    });

    let isSubmittingOrder = false;

    submitBtn?.addEventListener('click', async () => {
      if (isSubmittingOrder) return;

      const name = document.getElementById('orderName')?.value?.trim();
      const email = document.getElementById('orderEmail')?.value?.trim();

      // ✅ ВАЖНО: id в HTML = orderPhoneNumber
      const phoneRaw = document.getElementById('orderPhoneNumber')?.value?.trim();
      const phoneNumber = normalizePhone(phoneRaw); // ✅ переменная phoneNumber теперь есть

      if (!name) {
        showToast('Введите имя');
        return;
      }
      if (!isValidEmail(email)) {
        showToast('Введите корректный email');
        return;
      }
      if (!isValidPhone(phoneNumber)) {
        showToast('Введите телефон в формате: 8XXXXXXXXXX или +7XXXXXXXXXX');
        return;
      }

      const cart = getCart();
      const items = Object.values(cart.items || {});
      if (!items.length) {
        showToast('Корзина пустая');
        modal.style.display = 'none';
        return;
      }

      // ✅ payload с нужным полем phoneNumber
      const payload = {
        name,
        email,
        phoneNumber,
        items: items.map(it => ({
          productId: it.product?.id,
          name: it.product?.name,
          qty: Number(it.qty) || 1,
          price: Number(it.product?.price) || 0
        }))
      };

      modal.style.display = 'none';
      showToast('Отправляем заказ...');

      isSubmittingOrder = true;
      submitBtn.disabled = true;
      submitBtn.textContent = 'Отправляем...';

      try {
        const result = await createOrderOnBackend(payload);
        showToast(`Заказ оформлен! №${result.id}`);
        clearCart();
        updateCartBadge();
      } catch (e) {
        showToast(`Не удалось оформить заказ: ${e.message}`);
        modal.style.display = 'block';
      } finally {
        isSubmittingOrder = false;
        submitBtn.disabled = false;
        submitBtn.textContent = 'Отправить заказ';
      }
    });

  document.getElementById('cartList')?.addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-action]');
    if (!btn) return;

    const action = btn.dataset.action;
    const id = btn.dataset.id;

    const cart = getCart();
    const cur = Number(cart.items?.[id]?.qty) || 1;

    if (action === 'inc') updateQty(id, cur + 1);
    if (action === 'dec') updateQty(id, cur - 1);
    if (action === 'remove') removeItem(id);
  });

  document.getElementById('cartList')?.addEventListener('input', (e) => {
    const input = e.target.closest('input[data-action="qty"]');
    if (!input) return;
    updateQty(input.dataset.id, input.value);
  });

  document.getElementById('clearCartBtn')?.addEventListener('click', () => {
    clearCart();
    alert('Корзина очищена');
  });

  document.getElementById('checkoutBtn')?.addEventListener('click', () => {
    const cart = getCart();
    const items = Object.values(cart.items || {});
    if (!items.length) {
      showToast('Корзина пустая');
      return;
    }

    document.getElementById('checkoutHint').textContent =
      `Товаров в заказе: ${items.length} | Сумма: ${formatPrice(calcTotal(cart))}`;

    document.getElementById('checkoutModal').style.display = 'block';
  });


    updateCartBadge();
});
