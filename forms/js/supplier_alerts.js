/**
 * 購買先マスター一覧 視覚的アラート機能
 * Supplier Master List Visual Alerts
 * 
 * 再評価期限に基づいて購買先リストに色分け表示を行う
 * 
 * @author Supplier Quality Audit System Development Team
 * @version 1.0
 * @since 2025-11-20
 */

(function() {
    'use strict';

    /**
     * アラートレベルの定義
     */
    const AlertLevel = {
        CRITICAL: 'critical',    // 期限超過（赤色）
        WARNING: 'warning',      // 1ヶ月以内（黄色）
        NORMAL: 'normal'         // 通常（白色）
    };

    /**
     * アラートスタイルの定義
     */
    const AlertStyles = {
        critical: {
            backgroundColor: '#ffebee',
            color: '#c62828',
            borderLeft: '4px solid #c62828',
            fontWeight: 'bold'
        },
        warning: {
            backgroundColor: '#fff3e0',
            color: '#ef6c00',
            borderLeft: '4px solid #ef6c00',
            fontWeight: 'bold'
        },
        normal: {
            backgroundColor: '#ffffff',
            color: '#333333',
            borderLeft: 'none',
            fontWeight: 'normal'
        }
    };

    /**
     * 日付を比較してアラートレベルを判定
     * 
     * @param {Date|string} nextReviewDate 次回審査予定日
     * @return {string} アラートレベル
     */
    function determineAlertLevel(nextReviewDate) {
        if (!nextReviewDate) {
            return AlertLevel.NORMAL;
        }

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const reviewDate = new Date(nextReviewDate);
        reviewDate.setHours(0, 0, 0, 0);

        const oneMonthFromNow = new Date(today);
        oneMonthFromNow.setMonth(oneMonthFromNow.getMonth() + 1);

        if (reviewDate < today) {
            return AlertLevel.CRITICAL; // 期限超過
        } else if (reviewDate <= oneMonthFromNow) {
            return AlertLevel.WARNING; // 1ヶ月以内
        } else {
            return AlertLevel.NORMAL; // 通常
        }
    }

    /**
     * テーブル行にアラートスタイルを適用
     * 
     * @param {HTMLElement} row テーブル行要素
     * @param {string} alertLevel アラートレベル
     */
    function applyAlertStyle(row, alertLevel) {
        const style = AlertStyles[alertLevel];
        
        if (style) {
            row.style.backgroundColor = style.backgroundColor;
            row.style.color = style.color;
            row.style.borderLeft = style.borderLeft;
            row.style.fontWeight = style.fontWeight;
        }
    }

    /**
     * アラートバッジを生成
     * 
     * @param {string} alertLevel アラートレベル
     * @param {Date|string} nextReviewDate 次回審査予定日
     * @return {string} HTMLバッジ文字列
     */
    function generateAlertBadge(alertLevel, nextReviewDate) {
        if (alertLevel === AlertLevel.CRITICAL) {
            const daysOverdue = calculateDaysOverdue(nextReviewDate);
            return `<span class="alert-badge alert-critical">★期限超過 (${daysOverdue}日経過)★</span>`;
        } else if (alertLevel === AlertLevel.WARNING) {
            const daysRemaining = calculateDaysRemaining(nextReviewDate);
            return `<span class="alert-badge alert-warning">期限間近 (残り${daysRemaining}日)</span>`;
        } else {
            return '<span class="alert-badge alert-normal">-</span>';
        }
    }

    /**
     * 期限超過日数を計算
     * 
     * @param {Date|string} nextReviewDate 次回審査予定日
     * @return {number} 超過日数
     */
    function calculateDaysOverdue(nextReviewDate) {
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const reviewDate = new Date(nextReviewDate);
        reviewDate.setHours(0, 0, 0, 0);

        const diffTime = today - reviewDate;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        return diffDays;
    }

    /**
     * 残り日数を計算
     * 
     * @param {Date|string} nextReviewDate 次回審査予定日
     * @return {number} 残り日数
     */
    function calculateDaysRemaining(nextReviewDate) {
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const reviewDate = new Date(nextReviewDate);
        reviewDate.setHours(0, 0, 0, 0);

        const diffTime = reviewDate - today;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        return diffDays;
    }

    /**
     * 購買先マスター一覧テーブルにアラートを適用
     * 
     * @param {string} tableId テーブルのID
     * @param {string} dateColumnIndex 次回審査予定日の列インデックス
     */
    function applyAlertsToSupplierTable(tableId, dateColumnIndex) {
        const table = document.getElementById(tableId);
        if (!table) {
            console.error('Table not found: ' + tableId);
            return;
        }

        const tbody = table.querySelector('tbody');
        if (!tbody) {
            console.error('Table body not found');
            return;
        }

        const rows = tbody.querySelectorAll('tr');
        let criticalCount = 0;
        let warningCount = 0;

        rows.forEach(function(row) {
            const cells = row.querySelectorAll('td');
            if (cells.length > dateColumnIndex) {
                const dateCell = cells[dateColumnIndex];
                const nextReviewDate = dateCell.textContent.trim();

                if (nextReviewDate && nextReviewDate !== '-') {
                    const alertLevel = determineAlertLevel(nextReviewDate);
                    applyAlertStyle(row, alertLevel);

                    if (cells.length > dateColumnIndex + 1) {
                        const alertCell = cells[dateColumnIndex + 1];
                        alertCell.innerHTML = generateAlertBadge(alertLevel, nextReviewDate);
                    }

                    if (alertLevel === AlertLevel.CRITICAL) {
                        criticalCount++;
                    } else if (alertLevel === AlertLevel.WARNING) {
                        warningCount++;
                    }
                }
            }
        });

        updateSummaryCounts(criticalCount, warningCount);

        console.log('Alerts applied: ' + criticalCount + ' critical, ' + warningCount + ' warning');
    }

    /**
     * サマリーカウントを更新
     * 
     * @param {number} criticalCount 期限超過件数
     * @param {number} warningCount 期限間近件数
     */
    function updateSummaryCounts(criticalCount, warningCount) {
        const overdueElement = document.getElementById('overdueCount');
        if (overdueElement) {
            overdueElement.textContent = criticalCount;
            if (criticalCount > 0) {
                overdueElement.style.color = '#c62828';
            }
        }

        const dueSoonElement = document.getElementById('dueSoonCount');
        if (dueSoonElement) {
            dueSoonElement.textContent = warningCount;
            if (warningCount > 0) {
                dueSoonElement.style.color = '#ef6c00';
            }
        }
    }

    /**
     * ISO9001認証有効期限のアラート
     * 
     * @param {Date|string} expiryDate 有効期限
     * @return {string} アラートレベル
     */
    function determineIsoExpiryAlert(expiryDate) {
        if (!expiryDate) {
            return AlertLevel.NORMAL;
        }

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const expiry = new Date(expiryDate);
        expiry.setHours(0, 0, 0, 0);

        const threeMonthsFromNow = new Date(today);
        threeMonthsFromNow.setMonth(threeMonthsFromNow.getMonth() + 3);

        if (expiry < today) {
            return AlertLevel.CRITICAL; // 期限切れ
        } else if (expiry <= threeMonthsFromNow) {
            return AlertLevel.WARNING; // 3ヶ月以内
        } else {
            return AlertLevel.NORMAL;
        }
    }

    /**
     * ツールチップを表示
     * 
     * @param {HTMLElement} element 対象要素
     * @param {string} message ツールチップメッセージ
     */
    function showTooltip(element, message) {
        element.setAttribute('title', message);
        element.style.cursor = 'help';
    }

    /**
     * 購買先ステータスに応じた視覚的表示
     * 
     * @param {string} status ステータス（ACTIVE, SUSPENDED, INACTIVE）
     * @return {object} スタイル情報
     */
    function getStatusStyle(status) {
        const styles = {
            'ACTIVE': {
                color: '#4caf50',
                icon: '●',
                text: '有効'
            },
            'SUSPENDED': {
                color: '#f44336',
                icon: '■',
                text: '停止'
            },
            'INACTIVE': {
                color: '#9e9e9e',
                icon: '○',
                text: '無効'
            }
        };

        return styles[status] || styles['ACTIVE'];
    }

    /**
     * 等級に応じたバッジスタイル
     * 
     * @param {number} rating 等級（1, 2, 3）
     * @return {object} スタイル情報
     */
    function getRatingStyle(rating) {
        const styles = {
            1: {
                backgroundColor: '#4caf50',
                color: '#ffffff',
                text: '優良'
            },
            2: {
                backgroundColor: '#2196f3',
                color: '#ffffff',
                text: '良好'
            },
            3: {
                backgroundColor: '#ff9800',
                color: '#ffffff',
                text: '標準'
            }
        };

        return styles[rating] || {
            backgroundColor: '#9e9e9e',
            color: '#ffffff',
            text: '未評価'
        };
    }

    /**
     * ページ読み込み時の初期化処理
     */
    function initialize() {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', function() {
                applyAlertsToSupplierTable('supplierTable', 5); // 5列目が次回審査予定日
            });
        } else {
            applyAlertsToSupplierTable('supplierTable', 5);
        }
    }

    /**
     * 公開API
     */
    window.SupplierAlerts = {
        determineAlertLevel: determineAlertLevel,
        applyAlertStyle: applyAlertStyle,
        generateAlertBadge: generateAlertBadge,
        applyAlertsToSupplierTable: applyAlertsToSupplierTable,
        determineIsoExpiryAlert: determineIsoExpiryAlert,
        getStatusStyle: getStatusStyle,
        getRatingStyle: getRatingStyle,
        calculateDaysOverdue: calculateDaysOverdue,
        calculateDaysRemaining: calculateDaysRemaining
    };

    initialize();

})();
