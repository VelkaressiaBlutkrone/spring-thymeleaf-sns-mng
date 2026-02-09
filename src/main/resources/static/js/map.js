/**
 * Step 12: 지도 메인 페이지 - 카카오맵 연동, GPS 위치, 반경 내 Pin 마커, Pin 클릭 시 게시글 링크.
 * RULE 8: var 금지, const/let, 화살표 함수, async/await, try-catch.
 * RULE 1.5.6: 사용자 입력(description 등) 표시 시 textContent 사용, innerHTML 금지.
 */
(function () {
    'use strict';

    const API_BASE = window.MAP_API_BASE || '/api';
    const KAKAO_APP_KEY = window.MAP_KAKAO_APP_KEY || '';
    const DEFAULT_CENTER = { lat: 37.5665, lng: 126.9780 };
    const DEFAULT_RADIUS_KM = 5;

    const showPlaceholder = () => {
        const mapEl = document.getElementById('map');
        const ph = document.getElementById('map-placeholder');
        if (mapEl) mapEl.style.display = 'none';
        if (ph) ph.style.display = 'flex';
    };

    const fetchNearbyPins = async (lat, lng, radiusKm = DEFAULT_RADIUS_KM, page = 0, size = 50) => {
        const url = `${API_BASE}/pins/nearby?lat=${lat}&lng=${lng}&radiusKm=${radiusKm}&page=${page}&size=${size}`;
        const res = await fetch(url);
        if (!res.ok) throw new Error('Pin 조회 실패');
        return res.json();
    };

    const createInfoContent = (pin) => {
        const postsUrl = `/pins/${pin.id}/posts`;
        const desc = pin.description || '(설명 없음)';
        const div = document.createElement('div');
        div.className = 'info-window';
        const h4 = document.createElement('h4');
        h4.textContent = desc;
        div.appendChild(h4);
        const link = document.createElement('a');
        link.href = postsUrl;
        link.textContent = '게시글/이미지 보기 →';
        link.target = '_self';
        div.appendChild(link);
        return div;
    };

    const getCurrentPosition = () =>
        new Promise((resolve, reject) => {
            if (!navigator.geolocation) {
                reject(new Error('Geolocation not supported'));
                return;
            }
            navigator.geolocation.getCurrentPosition(
                (pos) => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
                reject,
                { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
            );
        });

    const initMap = () => {
        if (!KAKAO_APP_KEY || typeof kakao === 'undefined') {
            showPlaceholder();
            return;
        }

        kakao.maps.load(async () => {
            const container = document.getElementById('map');
            if (!container) return;

            const mapCenter = new kakao.maps.LatLng(DEFAULT_CENTER.lat, DEFAULT_CENTER.lng);
            const map = new kakao.maps.Map(container, { center: mapCenter, level: 6 });

            let userMarker = null;
            const markers = [];
            const infowindow = new kakao.maps.InfoWindow({ zIndex: 1 });

            const moveToUserPosition = (lat, lng) => {
                const pos = new kakao.maps.LatLng(lat, lng);
                map.setCenter(pos);
                if (userMarker) userMarker.setMap(null);
                userMarker = new kakao.maps.Marker({ position: pos, map });
            };

            const addPinMarkers = (pins) => {
                markers.forEach((m) => m.setMap(null));
                markers.length = 0;
                if (!pins?.content) return;
                pins.content.forEach((pin) => {
                    const lat = pin.latitude;
                    const lng = pin.longitude;
                    if (lat == null || lng == null) return;
                    const pos = new kakao.maps.LatLng(lat, lng);
                    const marker = new kakao.maps.Marker({ position: pos, map });
                    markers.push(marker);
                    kakao.maps.event.addListener(marker, 'click', () => {
                        infowindow.close();
                        infowindow.setContent(createInfoContent(pin));
                        infowindow.open(map, marker);
                    });
                });
            };

            const loadPins = async (lat, lng) => {
                try {
                    const data = await fetchNearbyPins(lat, lng);
                    addPinMarkers(data);
                } catch (err) {
                    if (typeof console !== 'undefined' && console.warn) {
                        console.warn('Pin 조회 실패:', err);
                    }
                }
            };

            try {
                const pos = await getCurrentPosition();
                moveToUserPosition(pos.lat, pos.lng);
                await loadPins(pos.lat, pos.lng);
            } catch {
                await loadPins(DEFAULT_CENTER.lat, DEFAULT_CENTER.lng);
            }
        });
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initMap);
    } else {
        initMap();
    }
})();
