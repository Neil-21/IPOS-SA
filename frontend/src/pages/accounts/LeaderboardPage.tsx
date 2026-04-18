/**
 * pages/accounts/LeaderboardPage.tsx
 * Ranks all merchants by total invoice value (highest spender first).
 * Displays a podium for the top three and a full ranked table below.
 *
 * Derived data (computed with useMemo):
 *   • totalSpend  — sum of all invoice totalAmounts for the merchant
 *   • orderCount  — number of orders placed
 * Ranks are assigned after sorting; ties share the same rank number.
 */
import React, { useMemo } from 'react';
import { useAppData } from '@/context/AppDataContext';
import { Trophy, TrendingUp, ShoppingBag, AlertCircle } from 'lucide-react';

export function LeaderboardPage() {
  const { merchants, orders, invoices } = useAppData();

  const ranked = useMemo(() => {
    return merchants
      .map((m) => {
        const merchantOrders = orders.filter((o) => o.merchantId === m.id);
        const totalSpend = invoices
          .filter((inv) => inv.merchantId === m.id)
          .reduce((sum, inv) => sum + (inv.totalAmount ?? 0), 0);
        const orderCount = merchantOrders.length;
        return { ...m, totalSpend, orderCount };
      })
      .sort((a, b) => b.totalSpend - a.totalSpend);
  }, [merchants, orders, invoices]);

  const medals = ['🥇', '🥈', '🥉'];

  return (
    <div style={{ padding: '32px', maxWidth: '900px' }}>
      {/* Header */}
      <div style={{ marginBottom: '28px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
          <Trophy size={22} color="var(--color-primary)" />
          <h1 style={{ fontSize: '20px', fontWeight: 700 }}>Merchant Leaderboard</h1>
        </div>
        <p style={{ color: 'var(--color-text-muted)', fontSize: '14px' }}>
          Merchants ranked by total spend across all invoices.
        </p>
      </div>

      {/* Top 3 Podium Cards */}
      {ranked.length >= 3 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px', marginBottom: '32px' }}>
          {ranked.slice(0, 3).map((m, i) => (
            <div
              key={m.id}
              style={{
                background: i === 0 ? 'linear-gradient(135deg, #fef3c7, #fde68a)' : 'var(--color-surface)',
                border: i === 0 ? '1px solid #f59e0b' : '1px solid var(--color-border)',
                borderRadius: '12px',
                padding: '20px',
                textAlign: 'center',
              }}
            >
              <div style={{ fontSize: '28px', marginBottom: '8px' }}>{medals[i]}</div>
              <p style={{ fontWeight: 700, fontSize: '14px', marginBottom: '4px' }}>{m.companyName ?? m.contactName}</p>
              <p style={{ fontSize: '20px', fontWeight: 800, color: i === 0 ? '#b45309' : 'var(--color-primary)' }}>
                £{m.totalSpend.toLocaleString('en-GB', { minimumFractionDigits: 2 })}
              </p>
              <p style={{ fontSize: '12px', color: 'var(--color-text-muted)', marginTop: '4px' }}>
                {m.orderCount} orders
              </p>
            </div>
          ))}
        </div>
      )}

      {/* Full Table */}
      <div style={{ background: 'var(--color-surface)', border: '1px solid var(--color-border)', borderRadius: '12px', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ background: 'var(--color-surface-offset)', borderBottom: '1px solid var(--color-border)' }}>
              <th style={th}>#</th>
              <th style={th}>Merchant</th>
              <th style={th}>Status</th>
              <th style={{ ...th, textAlign: 'right' }}>Orders</th>
              <th style={{ ...th, textAlign: 'right' }}>Total Spend</th>
            </tr>
          </thead>
          <tbody>
            {ranked.map((m, i) => (
              <tr
                key={m.id}
                style={{ borderBottom: '1px solid var(--color-border)', transition: 'background .15s' }}
                onMouseEnter={(e) => (e.currentTarget.style.background = 'var(--color-surface-offset)')}
                onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
              >
                <td style={{ ...td, fontWeight: 700, color: i < 3 ? 'var(--color-primary)' : 'var(--color-text-muted)', width: '48px' }}>
                  {i < 3 ? medals[i] : `#${i + 1}`}
                </td>
                <td style={td}>
                  <p style={{ fontWeight: 600, fontSize: '14px' }}>{m.companyName ?? m.contactName}</p>
                  <p style={{ fontSize: '12px', color: 'var(--color-text-muted)' }}>{m.iposAccount}</p>
                </td>
                <td style={td}>
                  <span style={{
                    fontSize: '11px', fontWeight: 600, padding: '2px 8px', borderRadius: '99px', textTransform: 'uppercase',
                    background: m.accountStatus === 'normal' ? '#d1fae5' : m.accountStatus === 'suspended' ? '#fee2e2' : '#fef3c7',
                    color: m.accountStatus === 'normal' ? '#065f46' : m.accountStatus === 'suspended' ? '#991b1b' : '#92400e',
                  }}>
                    {m.accountStatus}
                  </span>
                </td>
                <td style={{ ...td, textAlign: 'right', fontWeight: 600 }}>{m.orderCount}</td>
                <td style={{ ...td, textAlign: 'right', fontWeight: 700, fontVariantNumeric: 'tabular-nums' }}>
                  £{m.totalSpend.toLocaleString('en-GB', { minimumFractionDigits: 2 })}
                </td>
              </tr>
            ))}
            {ranked.length === 0 && (
              <tr>
                <td colSpan={5} style={{ padding: '40px', textAlign: 'center', color: 'var(--color-text-muted)' }}>
                  No merchant data available.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

const th: React.CSSProperties = {
  padding: '10px 16px',
  fontSize: '12px',
  fontWeight: 600,
  textAlign: 'left',
  color: 'var(--color-text-muted)',
  textTransform: 'uppercase',
  letterSpacing: '.04em',
};

const td: React.CSSProperties = {
  padding: '12px 16px',
  fontSize: '14px',
};
