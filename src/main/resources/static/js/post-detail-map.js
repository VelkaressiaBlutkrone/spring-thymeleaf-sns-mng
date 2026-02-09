/**
 * Step 13: 게시글 상세 페이지 - 지도 위치 표시, 사용자→목적지 거리 계산.
 * RULE 8: const/let, 화살표 함수, async/await.
 */
(function () {
    'use strict';

    const haversineKm = (lat1, lng1, lat2, lng2) => {
        const R = 6371;
        const dLat = (lat2 - lat1) * Math.PI / 180;
        const dLng = (lng2 - lng1) * Math.PI / 180;
        const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    };

    const initDetailMap = (options) => {
        const { containerId, lat, lng, distanceElId } = options;
        const container = document.getElementById(containerId);
        if (!container || lat == null || lng == null || typeof kakao === 'undefined') return;

        kakao.maps.load(() => {
            const map = new kakao.maps.Map(container, {
                center: new kakao.maps.LatLng(lat, lng),
                level: 4
            });
            new kakao.maps.Marker({ position: new kakao.maps.LatLng(lat, lng), map });
        });

        if (distanceElId && navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (pos) => {
                    const userLat = pos.coords.latitude;
                    const userLng = pos.coords.longitude;
                    const km = haversineKm(userLat, userLng, lat, lng);
                    const el = document.getElementById(distanceElId);
                    if (el) el.textContent = '현재 위치에서 약 ' + km.toFixed(1) + ' km';
                },
                () => {
                    const el = document.getElementById(distanceElId);
                    if (el) el.textContent = '위치 권한이 없어 거리를 표시할 수 없습니다.';
                }
            );
        }
    };

    window.initPostDetailMap = initDetailMap;
})();
