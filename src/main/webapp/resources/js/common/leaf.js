/* 잎새 캐릭터 · 페이지를 열 때마다 떨어지는 잎 · 프로필 메뉴 닫기.
   모두 장식이라 실패하거나 저장소를 못 써도 화면 기능에는 영향을 주지 않는다. */
var App = window.App || {};

(function(){
	var NS = 'http://www.w3.org/2000/svg',
		MOUTH = {happy: 'M57 80 Q66 89 75 80', soso: 'M58 83 L74 83', worried: 'M57 86 Q66 78 75 86'},

		/* SVG 요소 하나 */
		el = function(tag, attrs, kids){
			var n = document.createElementNS(NS, tag);
			Object.keys(attrs || {}).forEach(function(k){ n.setAttribute(k, attrs[k]); });
			(kids || []).forEach(function(k){ n.appendChild(k); });
			return n;
		},

		/* 사용자가 움직임 줄이기를 켰는지 */
		reduced = function(){
			return window.matchMedia && matchMedia('(prefers-reduced-motion: reduce)').matches;
		};

	/* App.leaf(mood): 표정(happy · soso · worried)이 있는 잎새 SVG. 크기는 CSS 가 정한다 */
	App.leaf = function(mood){
		var line = 'var(--leaf-line)',
			svg = el('svg', {viewBox: '0 0 132 132', 'aria-hidden': 'true', focusable: 'false'}),
			body = el('g', {'class': 'leaf-body'}, [
				el('path', {d: 'M66 124 C66 112 66 104 66 98', fill: 'none', stroke: line, 'stroke-width': '5', 'stroke-linecap': 'round'}),
				el('path', {d: 'M66 12 C98 22 114 50 108 76 C102 102 82 112 66 112 C50 112 30 102 24 76 C18 50 34 22 66 12 Z', fill: 'var(--leaf)', stroke: line, 'stroke-width': '4', 'stroke-linejoin': 'round'}),
				el('path', {d: 'M66 26 C70 40 70 56 66 70', fill: 'none', stroke: line, 'stroke-width': '3', 'stroke-linecap': 'round', opacity: '.35'}),
				el('ellipse', {cx: '44', cy: '80', rx: '8', ry: '5', fill: 'var(--blush)', opacity: '.8'}),
				el('ellipse', {cx: '88', cy: '80', rx: '8', ry: '5', fill: 'var(--blush)', opacity: '.8'}),
				el('g', {'class': 'leaf-eyes'}, [
					el('circle', {cx: '52', cy: '68', r: '5.5', fill: 'var(--ink)'}),
					el('circle', {cx: '80', cy: '68', r: '5.5', fill: 'var(--ink)'}),
					el('circle', {cx: '54', cy: '66', r: '1.8', fill: '#fff'}),
					el('circle', {cx: '82', cy: '66', r: '1.8', fill: '#fff'})
				]),
				el('path', {d: MOUTH[mood] || MOUTH.happy, fill: 'none', stroke: 'var(--ink)', 'stroke-width': '4', 'stroke-linecap': 'round'})
			]);
		if(mood === 'worried'){
			body.appendChild(el('path', {d: 'M100 44 C104 52 106 56 102 59 C98 61 95 57 97 52 Z', fill: '#9fd0ff', stroke: '#5b9bd5', 'stroke-width': '2'}));
		}
		svg.appendChild(body);
		return svg;
	};

	/* App.leafRain(): 잎이 하늘에서 살랑살랑 떨어진다. 페이지를 열 때마다(새로고침·이동 포함), 동작 줄이기 설정이면 생략.
	   클릭을 막지 않으므로(pointer-events: none) 떨어지는 동안에도 바로 쓸 수 있다 */
	App.leafRain = function(){
		if(reduced() || !document.body.animate) return;
		var layer = document.createElement('div'),
			W = window.innerWidth, H = window.innerHeight,
			n = W < 600 ? 10 : 18,
			fills = ['var(--leaf)', 'var(--leaf)', 'var(--accent)', 'color-mix(in srgb, var(--leaf) 55%, white)'],
			left = n, k;
		layer.className = 'leaf-rain';
		layer.setAttribute('aria-hidden', 'true');
		document.body.appendChild(layer);
		for(k = 0; k < n; k++){
			(function(k){
				var size = 16 + Math.random() * 18,
					sway = (24 + Math.random() * 46) * (Math.random() < .5 ? -1 : 1),
					spin = (Math.random() < .5 ? -1 : 1) * (200 + Math.random() * 320),
					leaf = el('svg', {viewBox: '0 0 24 24', width: String(size), height: String(size)}, [
						el('path', {d: 'M12 2C18 5 21 10 20 15C19 20 15 22 12 22C9 22 5 20 4 15C3 10 6 5 12 2Z', fill: fills[k % fills.length], stroke: 'var(--leaf-line)', 'stroke-width': '1.6', 'stroke-linejoin': 'round'}),
						el('path', {d: 'M12 6V19', stroke: 'var(--leaf-line)', 'stroke-width': '1.4', 'stroke-linecap': 'round', opacity: '.5'})
					]),
					frames = [0, .25, .5, .75, 1].map(function(t, i){
						var x = i === 0 ? 0 : (i % 2 ? sway : -sway * .5);
						return {transform: 'translate(' + x + 'px,' + (t * (H + 96)) + 'px) rotate(' + (spin * t) + 'deg) rotateY(' + (t * 540) + 'deg)', opacity: t < .85 ? 1 : 0, offset: t};
					}),
					anim;
				leaf.style.left = (Math.random() * (W - size)) + 'px';
				layer.appendChild(leaf);
				anim = leaf.animate(frames, {duration: 2600 + Math.random() * 2000, delay: Math.random() * 1100, easing: 'cubic-bezier(.37, .1, .63, .9)', fill: 'backwards'});
				anim.onfinish = anim.oncancel = function(){
					leaf.remove();
					if(--left === 0) layer.remove();
				};
			}(k));
		}
	};

	document.addEventListener('DOMContentLoaded', function(){
		/* data-leaf="happy" 자리에 캐릭터를 넣는다(로고 등) */
		document.querySelectorAll('[data-leaf]').forEach(function(box){
			box.replaceChildren(App.leaf(box.getAttribute('data-leaf')));
		});
		App.leafRain();
	});

	/* 프로필 메뉴(details): 바깥을 누르거나 Escape 면 닫는다 */
	document.addEventListener('click', function(e){
		document.querySelectorAll('.profile-menu[open]').forEach(function(m){
			if(!m.contains(e.target)) m.removeAttribute('open');
		});
	});
	document.addEventListener('keydown', function(e){
		if(e.key !== 'Escape') return;
		document.querySelectorAll('.profile-menu[open]').forEach(function(m){
			m.removeAttribute('open');
			var s = m.querySelector('summary');
			if(s) s.focus();
		});
	});
}());
