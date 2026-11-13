/* VSK Tea - shared behaviour. Plain JavaScript, no libraries. */
(function () {
	'use strict';

	var $ = function (sel, root) { return (root || document).querySelector(sel); };
	var $$ = function (sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); };
	var rupees = function (n) { return '₹ ' + Number(n).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); };

	function store(key, value) {
		try {
			if (value === undefined) { return localStorage.getItem(key); }
			localStorage.setItem(key, value);
		} catch (e) { return null; }
	}

	/* ---------- theme ---------- */
	function applyTheme(t) {
		document.documentElement.setAttribute('data-theme', t);
		var btn = $('#theme-toggle');
		if (btn) {
			btn.textContent = t === 'dark' ? '\u2600\uFE0F' : '\uD83C\uDF19';
			btn.setAttribute('aria-label', t === 'dark' ? 'Switch to light theme' : 'Switch to dark theme');
		}
	}
	var themeBtn = $('#theme-toggle');
	applyTheme(document.documentElement.getAttribute('data-theme') || 'light');
	if (themeBtn) {
		themeBtn.addEventListener('click', function () {
			var next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
			applyTheme(next);
			store('theme', next);
		});
	}

	/* ---------- mobile nav ---------- */
	var navToggle = $('#nav-toggle'), navLinks = $('#nav-links');
	if (navToggle && navLinks) {
		navToggle.addEventListener('click', function () {
			var open = navLinks.classList.toggle('open');
			navToggle.setAttribute('aria-expanded', open ? 'true' : 'false');
			navToggle.textContent = open ? '\u2715' : '\u2630';
		});
		document.addEventListener('click', function (e) {
			if (!navLinks.contains(e.target) && !navToggle.contains(e.target)) { navLinks.classList.remove('open'); }
		});
	}

	/* ---------- toasts ---------- */
	var toastBox = $('#toasts');
	function showToast(message, type) {
		if (!toastBox || !message) { return; }
		type = type || 'info';
		var icon = { success: 'fa-circle-check', error: 'fa-circle-exclamation', info: 'fa-circle-info' }[type] || 'fa-circle-info';
		var el = document.createElement('div');
		el.className = 'toast ' + type;
		el.setAttribute('role', type === 'error' ? 'alert' : 'status');
		var i = document.createElement('i'); i.className = 'fa-solid ' + icon;
		var span = document.createElement('span'); span.textContent = message;
		var close = document.createElement('button'); close.type = 'button'; close.setAttribute('aria-label', 'Dismiss'); close.innerHTML = '&times;';
		el.appendChild(i); el.appendChild(span); el.appendChild(close);
		toastBox.appendChild(el);
		var remove = function () { el.classList.add('leaving'); setTimeout(function () { el.remove(); }, 260); };
		close.addEventListener('click', remove);
		setTimeout(remove, 5000);
	}
	window.showToast = showToast;
	var toastData = $('#toast-data');
	if (toastData && toastData.dataset.message) { showToast(toastData.dataset.message, toastData.dataset.type); }

	/* ---------- confirm dialog for destructive actions ---------- */
	var confirmBackdrop = null, confirmResolve = null;
	function buildConfirm() {
		confirmBackdrop = document.createElement('div');
		confirmBackdrop.className = 'modal-backdrop';
		confirmBackdrop.innerHTML =
			'<div class="modal card" role="dialog" aria-modal="true" aria-labelledby="confirm-title">' +
			'<h3 id="confirm-title">Are you sure?</h3><p class="muted" id="confirm-text"></p>' +
			'<div class="modal-actions"><button type="button" class="btn btn-ghost" data-no>Cancel</button>' +
			'<button type="button" class="btn btn-danger" data-yes>Yes, continue</button></div></div>';
		document.body.appendChild(confirmBackdrop);
		confirmBackdrop.addEventListener('click', function (e) {
			if (e.target === confirmBackdrop || e.target.hasAttribute('data-no')) { closeConfirm(false); }
			if (e.target.hasAttribute('data-yes')) { closeConfirm(true); }
		});
	}
	function closeConfirm(result) {
		confirmBackdrop.classList.remove('open');
		if (confirmResolve) { confirmResolve(result); confirmResolve = null; }
	}
	function askConfirm(message) {
		if (!confirmBackdrop) { buildConfirm(); }
		$('#confirm-text').textContent = message;
		confirmBackdrop.classList.add('open');
		$('[data-no]', confirmBackdrop).focus();
		return new Promise(function (resolve) { confirmResolve = resolve; });
	}
	document.addEventListener('submit', function (e) {
		var form = e.target;
		var message = form.getAttribute('data-confirm');
		if (!message || form.dataset.confirmed) { return; }
		e.preventDefault();
		askConfirm(message).then(function (ok) {
			if (ok) { form.dataset.confirmed = '1'; form.submit(); }
		});
	}, true);

	/* ---------- Escape closes any modal ---------- */
	document.addEventListener('keydown', function (e) {
		if (e.key !== 'Escape') { return; }
		if (confirmBackdrop && confirmBackdrop.classList.contains('open')) { closeConfirm(false); }
		var om = $('#order-modal');
		if (om && om.classList.contains('open')) { closeOrder(); }
	});

	/* ---------- tabs ---------- */
	$$('[data-tabs]').forEach(function (group) {
		var tabs = $$('[role="tab"]', group);
		var useHash = group.hasAttribute('data-tabs-hash');
		function select(id, push) {
			tabs.forEach(function (t) {
				var on = t.getAttribute('aria-controls') === id;
				t.setAttribute('aria-selected', on ? 'true' : 'false');
				var panel = document.getElementById(t.getAttribute('aria-controls'));
				if (panel) { panel.hidden = !on; }
			});
			if (useHash && push) { history.replaceState(null, '', '#' + id); }
		}
		tabs.forEach(function (t) {
			t.addEventListener('click', function () { select(t.getAttribute('aria-controls'), true); });
		});
		var start = useHash && location.hash ? location.hash.slice(1) : null;
		var valid = tabs.some(function (t) { return t.getAttribute('aria-controls') === start; });
		var initial = valid ? start : (tabs.filter(function (t) { return t.getAttribute('aria-selected') === 'true'; })[0] || tabs[0]).getAttribute('aria-controls');
		select(initial, false);
		if (useHash) { window.addEventListener('hashchange', function () { var h = location.hash.slice(1); if (tabs.some(function (t) { return t.getAttribute('aria-controls') === h; })) { select(h, false); } }); }
	});

	/* ---------- live search ---------- */
	$$('input[data-filter]').forEach(function (input) {
		var container = $(input.getAttribute('data-filter'));
		if (!container) { return; }
		var emptyMsg = document.querySelector('[data-empty="' + input.getAttribute('data-filter') + '"]');
		function run() {
			var q = input.value.trim().toLowerCase(), shown = 0;
			$$('[data-search]', container).forEach(function (item) {
				var match = !q || item.getAttribute('data-search').toLowerCase().indexOf(q) !== -1;
				item.hidden = !match;
				if (match) { shown++; }
			});
			if (emptyMsg) { emptyMsg.hidden = shown !== 0; }
		}
		input.addEventListener('input', run);
	});

	/* ---------- sort select for card grids ---------- */
	$$('select[data-sort-target]').forEach(function (select) {
		var container = $(select.getAttribute('data-sort-target'));
		if (!container) { return; }
		var original = $$('[data-search]', container);
		select.addEventListener('change', function () {
			var items = original.slice();
			var v = select.value;
			if (v === 'price-asc') { items.sort(function (a, b) { return a.dataset.price - b.dataset.price; }); }
			else if (v === 'price-desc') { items.sort(function (a, b) { return b.dataset.price - a.dataset.price; }); }
			else if (v === 'name') { items.sort(function (a, b) { return a.dataset.name.localeCompare(b.dataset.name); }); }
			items.forEach(function (el) { container.appendChild(el); });
		});
	});

	/* ---------- sortable tables ---------- */
	$$('table').forEach(function (table) {
		var heads = $$('th[data-sort]', table);
		heads.forEach(function (th) {
			th.addEventListener('click', function () {
				var index = Array.prototype.indexOf.call(th.parentNode.children, th);
				var dir = th.classList.contains('asc') ? 'desc' : 'asc';
				heads.forEach(function (h) { h.classList.remove('asc', 'desc'); });
				th.classList.add(dir);
				var numeric = th.getAttribute('data-sort') === 'number';
				var body = table.tBodies[0];
				var rows = $$('tr', body).filter(function (r) { return !r.hasAttribute('data-empty-row'); });
				rows.sort(function (a, b) {
					var x = a.children[index].getAttribute('data-value') || a.children[index].textContent.trim();
					var y = b.children[index].getAttribute('data-value') || b.children[index].textContent.trim();
					var r = numeric ? (parseFloat(x) || 0) - (parseFloat(y) || 0) : x.localeCompare(y, undefined, { numeric: true, sensitivity: 'base' });
					return dir === 'asc' ? r : -r;
				});
				rows.forEach(function (r) { body.appendChild(r); });
			});
		});
	});

	/* ---------- auto-submit selects (order status) ---------- */
	$$('select[data-autosubmit]').forEach(function (sel) {
		sel.addEventListener('change', function () { sel.form.submit(); });
	});

	/* ---------- password show/hide + strength ---------- */
	$$('[data-toggle-password]').forEach(function (btn) {
		btn.addEventListener('click', function () {
			var input = btn.parentNode.querySelector('input');
			var show = input.type === 'password';
			input.type = show ? 'text' : 'password';
			btn.textContent = show ? 'Hide' : 'Show';
			btn.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
		});
	});
	$$('input[data-strength]').forEach(function (input) {
		var bar = $(input.getAttribute('data-strength'));
		if (!bar) { return; }
		input.addEventListener('input', function () {
			var v = input.value, score = 0;
			if (v.length >= 6) { score++; }
			if (v.length >= 10) { score++; }
			if (/[A-Z]/.test(v) && /[a-z]/.test(v)) { score++; }
			if (/\d/.test(v) && /[^A-Za-z0-9]/.test(v)) { score++; }
			var colors = ['#c93a3a', '#e8a317', '#9aba2f', '#2f7d3b'];
			var inner = bar.firstElementChild;
			inner.style.width = (v ? Math.max(score, 1) * 25 : 0) + '%';
			inner.style.background = colors[Math.max(score - 1, 0)];
		});
	});
	/* confirm-password match hint */
	var pw = $('input[data-match-source]');
	var pw2 = $('input[data-match-target]');
	if (pw && pw2) {
		var check = function () {
			var hint = $('#match-hint');
			if (!hint) { return; }
			if (!pw2.value) { hint.textContent = ''; return; }
			var ok = pw.value === pw2.value;
			hint.textContent = ok ? 'Passwords match' : 'Passwords do not match';
			hint.style.color = ok ? 'var(--primary)' : 'var(--danger)';
		};
		pw.addEventListener('input', check); pw2.addEventListener('input', check);
	}

	/* ---------- prevent double submit + spinner ---------- */
	document.addEventListener('submit', function (e) {
		var form = e.target;
		if (e.defaultPrevented || form.hasAttribute('data-no-spinner')) { return; }
		var btn = $('button[type="submit"]:not([data-no-spinner])', form);
		if (btn && !btn.disabled) {
			setTimeout(function () {
				btn.disabled = true;
				btn.setAttribute('data-label', btn.innerHTML);
				btn.innerHTML = '<span class="spinner"></span> Please wait...';
			}, 0);
		}
	});
	window.addEventListener('pageshow', function (e) {
		if (!e.persisted) { return; }
		$$('button[data-label]').forEach(function (b) { b.disabled = false; b.innerHTML = b.getAttribute('data-label'); });
	});

	/* ---------- order modal (Shop page) ---------- */
	var orderModal = $('#order-modal');
	var qtyInput = $('#order-qty');
	var unitPrice = 0;
	function updateTotal() {
		var q = parseInt(qtyInput.value, 10);
		if (isNaN(q) || q < 1) { q = 0; }
		$('#order-total').textContent = rupees(unitPrice * q);
	}
	function closeOrder() { orderModal.classList.remove('open'); document.body.style.overflow = ''; }
	if (orderModal && qtyInput) {
		$$('[data-order]').forEach(function (btn) {
			btn.addEventListener('click', function () {
				unitPrice = parseFloat(btn.dataset.price);
				$('#order-product-id').value = btn.dataset.id;
				$('#order-name').textContent = btn.dataset.name;
				$('#order-price').textContent = rupees(unitPrice) + ' / kg';
				qtyInput.value = 1;
				updateTotal();
				orderModal.classList.add('open');
				document.body.style.overflow = 'hidden';
				qtyInput.focus(); qtyInput.select();
			});
		});
		$('#qty-minus').addEventListener('click', function () { qtyInput.value = Math.max(1, (parseInt(qtyInput.value, 10) || 1) - 1); updateTotal(); });
		$('#qty-plus').addEventListener('click', function () { qtyInput.value = Math.min(1000, (parseInt(qtyInput.value, 10) || 0) + 1); updateTotal(); });
		qtyInput.addEventListener('input', updateTotal);
		orderModal.addEventListener('click', function (e) { if (e.target === orderModal || e.target.hasAttribute('data-close')) { closeOrder(); } });
	}

	/* ---------- count-up numbers ---------- */
	function countUp(el) {
		var target = parseFloat(el.dataset.count), money = el.hasAttribute('data-money');
		if (isNaN(target)) { return; }
		var start = performance.now(), dur = 900;
		function frame(now) {
			var p = Math.min((now - start) / dur, 1), v = target * (1 - Math.pow(1 - p, 3));
			el.textContent = money ? '\u20B9 ' + Math.round(v).toLocaleString('en-IN') : Math.round(v).toLocaleString('en-IN');
			if (p < 1) { requestAnimationFrame(frame); }
		}
		requestAnimationFrame(frame);
	}
	$$('[data-count]').forEach(countUp);

	/* ---------- scroll reveal ---------- */
	var reveals = $$('.reveal');
	if ('IntersectionObserver' in window) {
		var io = new IntersectionObserver(function (entries) {
			entries.forEach(function (en) { if (en.isIntersecting) { en.target.classList.add('visible'); io.unobserve(en.target); } });
		}, { threshold: 0.12 });
		reveals.forEach(function (el) { io.observe(el); });
	} else {
		reveals.forEach(function (el) { el.classList.add('visible'); });
	}
})();
