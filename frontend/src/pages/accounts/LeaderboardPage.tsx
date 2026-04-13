import { useMemo } from 'react';
import { Trophy, TrendingUp, ShoppingCart, CreditCard } from 'lucide-react';
import { Page } from '@/components/Layout/Header';
import { Card } from '@/components/ui/Card';
import { useAppData } from '@/context/AppDataContext';

type SortKey = 'totalSpend' | 'orderCount' | 'paymentScore';

function getMedal(rank: number) {
  if (rank === 1) return { emoji: '🥇', color: '#f59e0b', bg: 'rgba(245,158,11,.12)' };
  if (rank === 2) return { emoji: '🥈', color: '#9ca3af', bg: 'rgba(156,163,175,.12)' };
  if (rank === 3) return { emoji: '🥉', color: '#b45309', bg: 'rgba(180,83,9,.12)' };
  return { emoji: String(rank), color: 'var(--color-text-3)', bg: 'transparent' };
}

function ScoreBadge({ score }: { score: number }) {
  const color = score >= 80 ? '#6daa45' : score >= 50 ? '#e8af34' : '#a13544';
  const label = score >= 80 ? 'Excellent' : score >= 50 ? 'Average' : 'Poor';
  return (
    <span style={{
      fontSize: '11px', fontWeight: 700, padding: '2px 8px',
      borderRadius: '99px', background: `${color}22`, color,
    }}>
      {label} ({score})
    </span>
  );
}

export default function LeaderboardPage() {
  const { orders, invoices, merchants } = useAppData();

  const leaderboard = useMemo(() => {
    return merchants.map(merchant => {
      const merchantOrders = orders.filter(o => o.accountId === merchant.id);
      const merchantInvoices = invoices.filter(i => i.merchantId === merchant.id);

      const totalSpend = merchantOrders.reduce((s, o) => s + o.totalAmount, 0);
      const orderCount = merchantOrders.length;

      // Payment score: % of invoices that are fully PAID (0-100)
      const paidCount = merchantInvoices.filter(i => i.paymentStatus === 'PAID').length;
      const paymentScore = merchantInvoices.length > 0
        ? Math.round((paidCount / merchantInvoices.length) * 100)
        : 0;

      return {
        id: merchant.id,
        name: merchant.companyName,
        contactName: merchant.contactName,
        status: merchant.accountStatus,
        totalSpend,
        orderCount,
        paymentScore,
        invoiceCount: merchantInvoices.length,
      };
    });
  }, [orders, invoices, merchants]);

  const sorted = useMemo(() =>
    [...leaderboard].sort((a, b) => b.totalSpend - a.totalSpend),
    [leaderboard]
  );

  const totalSystemRevenue = sorted.reduce((s, m) => s + m.totalSpend, 0);

  return (
    <Page title="Merchant Leaderboard">

      {/* Top 3 Podium */}
      {sorted.length >= 3 && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px', marginBottom: '24px' }}>
          {[sorted[1], sorted[0], sorted[2]].map((merchant, i) => {
            const actualRank = i === 0 ? 2 : i === 1 ? 1 : 3;
            const medal = getMedal(actualRank);
            const heightMap = ['160px', '190px', '140px'];
            return (
              <Card key={merchant.id} padding="0">
                <div style={{
                  display: 'flex', flexDirection: 'column', alignItems: 'center',
                  justifyContent: 'flex-end', padding: '20px 16px',
                  minHeight: heightMap[i],
                  background: medal.bg,
                  borderRadius: 'var(--radius-md)',
                  textAlign: 'center',
                }}>
                  <div style={{ fontSize: '32px', marginBottom: '8px' }}>{medal.emoji}</div>
                  <p style={{ fontWeight: 700, fontSize: '14px', color: medal.color }}>{merchant.name}</p>
                  <p style={{ fontSize: '11px', color: 'var(--color-text-3)', marginTop: '2px' }}>{merchant.contactName}</p>
                  <p style={{ fontSize: '18px', fontWeight: 800, fontFamily: 'var(--font-mono)', marginTop: '8px', color: 'var(--color-text)' }}>
                    £{merchant.totalSpend.toLocaleString('en-GB', { minimumFractionDigits: 2 })}
                  </p>
                  <p style={{ fontSize: '11px', color: 'var(--color-text-3)', marginTop: '2px' }}>
                    {merchant.orderCount} orders
                  </p>
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* Full Rankings Table */}
      <Card padding="0">
        <div style={{ padding: '14px 16px', borderBottom: '1px solid var(--color-border)', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Trophy size={15} color="var(--color-primary)" />
          <p style={{ fontWeight: 700, fontSize: '13px' }}>Full Rankings</p>
        </div>

        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--color-border)' }}>
                {['Rank', 'Merchant', 'Total Spend', 'Share of Revenue', 'Orders', 'Payment Score', 'Status'].map(h => (
                  <th key={h} style={{
                    padding: '10px 16px', fontSize: '11px', fontWeight: 600,
                    color: 'var(--color-text-3)', textAlign: 'left',
                    textTransform: 'uppercase', letterSpacing: '0.05em',
                    whiteSpace: 'nowrap',
                  }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {sorted.map((merchant, index) => {
                const rank = index + 1;
                const medal = getMedal(rank);
                const revenueShare = totalSystemRevenue > 0
                  ? ((merchant.totalSpend / totalSystemRevenue) * 100).toFixed(1)
                  : '0.0';
                const statusColor = merchant.status === 'NORMAL' ? '#6daa45' : merchant.status === 'SUSPENDED' ? '#a13544' : '#e8af34';
                return (
                  <tr key={merchant.id} style={{ borderBottom: '1px solid var(--color-border)' }}
                    onMouseEnter={e => (e.currentTarget.style.background = 'var(--color-surface-offset)')}
                    onMouseLeave={e => (e.currentTarget.style.background = 'transparent')}
                  >
                    <td style={{ padding: '12px 16px' }}>
                      <span style={{
                        display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                        width: '28px', height: '28px', borderRadius: '50%',
                        background: medal.bg, color: medal.color,
                        fontSize: rank <= 3 ? '16px' : '12px', fontWeight: 700,
                      }}>
                        {medal.emoji}
                      </span>
                    </td>
                    <td style={{ padding: '12px 16px' }}>
                      <p style={{ fontWeight: 600, fontSize: '13px' }}>{merchant.name}</p>
                      <p style={{ fontSize: '11px', color: 'var(--color-text-3)', marginTop: '1px' }}>{merchant.contactName}</p>
                    </td>
                    <td style={{ padding: '12px 16px', fontFamily: 'var(--font-mono)', fontWeight: 700, fontSize: '13px' }}>
                      £{merchant.totalSpend.toLocaleString('en-GB', { minimumFractionDigits: 2 })}
                    </td>
                    <td style={{ padding: '12px 16px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <div style={{ width: '80px', height: '6px', background: 'var(--color-border)', borderRadius: '99px', overflow: 'hidden' }}>
                          <div style={{ width: `${revenueShare}%`, height: '100%', background: 'var(--color-primary)', borderRadius: '99px' }} />
                        </div>
                        <span style={{ fontSize: '12px', color: 'var(--color-text-2)', fontFamily: 'var(--font-mono)' }}>{revenueShare}%</span>
                      </div>
                    </td>
                    <td style={{ padding: '12px 16px', fontSize: '13px', fontFamily: 'var(--font-mono)' }}>
                      {merchant.orderCount}
                    </td>
                    <td style={{ padding: '12px 16px' }}>
                      <ScoreBadge score={merchant.paymentScore} />
                    </td>
                    <td style={{ padding: '12px 16px' }}>
                      <span style={{
                        fontSize: '11px', fontWeight: 600, padding: '2px 8px',
                        borderRadius: '99px', background: `${statusColor}22`, color: statusColor,
                        textTransform: 'capitalize',
                      }}>
                        {merchant.status.toLowerCase().replace('_', ' ')}
                      </span>
                    </td>
                  </tr>
                );
              })}
              {sorted.length === 0 && (
                <tr>
                  <td colSpan={7} style={{ padding: '32px', textAlign: 'center', fontSize: '13px', color: 'var(--color-text-3)' }}>
                    No merchant data available.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </Card>
    </Page>
  );
}
