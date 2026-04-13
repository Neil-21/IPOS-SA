import { useMemo } from 'react';
import { TrendingUp, AlertTriangle, CheckCircle, XCircle, CreditCard, ShoppingCart, FileText } from 'lucide-react';
import { Page } from '@/components/Layout/Header';
import { Card } from '@/components/ui/Card';
import { useAppData } from '@/context/AppDataContext';

function getScoreColor(score: number) {
  if (score >= 75) return '#6daa45';
  if (score >= 50) return '#e8af34';
  return '#a13544';
}

function getScoreLabel(score: number) {
  if (score >= 75) return 'Healthy';
  if (score >= 50) return 'At Risk';
  return 'Critical';
}

function getScoreIcon(score: number) {
  if (score >= 75) return <CheckCircle size={16} color="#6daa45" />;
  if (score >= 50) return <AlertTriangle size={16} color="#e8af34" />;
  return <XCircle size={16} color="#a13544" />;
}

function ScoreRing({ score }: { score: number }) {
  const color = getScoreColor(score);
  const radius = 36;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (score / 100) * circumference;
  return (
    <div style={{ position: 'relative', width: 88, height: 88, flexShrink: 0 }}>
      <svg width="88" height="88" style={{ transform: 'rotate(-90deg)' }}>
        <circle cx="44" cy="44" r={radius} fill="none" stroke="var(--color-border)" strokeWidth="7" />
        <circle cx="44" cy="44" r={radius} fill="none" stroke={color} strokeWidth="7"
          strokeDasharray={circumference} strokeDashoffset={offset}
          strokeLinecap="round" style={{ transition: 'stroke-dashoffset 0.6s ease' }}
        />
      </svg>
      <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        <span style={{ fontSize: '18px', fontWeight: 800, fontFamily: 'var(--font-mono)', color }}>{score}</span>
        <span style={{ fontSize: '9px', color: 'var(--color-text-3)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>/ 100</span>
      </div>
    </div>
  );
}

export default function ScorecardPage() {
  const { orders, invoices, merchants } = useAppData();

  const scorecards = useMemo(() => {
    return merchants.map(merchant => {
      const merchantOrders  = orders.filter(o => o.accountId === merchant.id);
      const merchantInvoices = invoices.filter(i => i.merchantId === merchant.id);

      // 1. Payment score (40pts): % of invoices paid
      const paidCount = merchantInvoices.filter(i => i.paymentStatus === 'PAID').length;
      const paymentScore = merchantInvoices.length > 0
        ? Math.round((paidCount / merchantInvoices.length) * 40)
        : 0;

      // 2. Credit utilisation score (30pts): lower utilisation = better
      const creditLimit = merchant.creditLimit ?? 1;
      const balance = merchant.currentBalance ?? 0;
      const utilisation = Math.min(balance / creditLimit, 1);
      const creditScore = Math.round((1 - utilisation) * 30);

      // 3. Order activity score (30pts): more orders = better (cap at 10)
      const orderScore = Math.min(merchantOrders.length, 10) * 3;

      const totalScore = paymentScore + creditScore + orderScore;

      const pendingInvoices = merchantInvoices.filter(i => i.paymentStatus !== 'PAID').length;
      const totalSpend = merchantOrders.reduce((s, o) => s + o.totalAmount, 0);

      return {
        id: merchant.id,
        name: merchant.companyName,
        contactName: merchant.contactName,
        status: merchant.accountStatus,
        creditLimit,
        balance,
        utilisation: Math.round(utilisation * 100),
        totalScore,
        paymentScore,
        creditScore,
        orderScore,
        orderCount: merchantOrders.length,
        pendingInvoices,
        totalSpend,
        invoiceCount: merchantInvoices.length,
      };
    }).sort((a, b) => b.totalScore - a.totalScore);
  }, [orders, invoices, merchants]);

  return (
    <Page title="Merchant Health Scorecards">

      {/* Summary bar */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '12px', marginBottom: '24px' }}>
        {[
          { label: 'Healthy', count: scorecards.filter(s => s.totalScore >= 75).length, color: '#6daa45' },
          { label: 'At Risk', count: scorecards.filter(s => s.totalScore >= 50 && s.totalScore < 75).length, color: '#e8af34' },
          { label: 'Critical', count: scorecards.filter(s => s.totalScore < 50).length, color: '#a13544' },
        ].map(item => (
          <Card key={item.label} padding="0">
            <div style={{ padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div style={{ width: 10, height: 10, borderRadius: '50%', background: item.color, flexShrink: 0 }} />
              <div>
                <p style={{ fontSize: '22px', fontWeight: 800, fontFamily: 'var(--font-mono)', color: item.color }}>{item.count}</p>
                <p style={{ fontSize: '11px', color: 'var(--color-text-3)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{item.label}</p>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Scorecards grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: '16px' }}>
        {scorecards.map(merchant => {
          const color = getScoreColor(merchant.totalScore);
          const statusColor = merchant.status === 'NORMAL' ? '#6daa45' : merchant.status === 'SUSPENDED' ? '#a13544' : '#e8af34';
          return (
            <Card key={merchant.id} padding="0">
              {/* Header */}
              <div style={{ padding: '16px', borderBottom: '1px solid var(--color-border)', display: 'flex', alignItems: 'center', gap: '12px' }}>
                <ScoreRing score={merchant.totalScore} />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '2px' }}>
                    {getScoreIcon(merchant.totalScore)}
                    <p style={{ fontWeight: 700, fontSize: '14px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {merchant.name}
                    </p>
                  </div>
                  <p style={{ fontSize: '11px', color: 'var(--color-text-3)' }}>{merchant.contactName}</p>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '6px' }}>
                    <span style={{ fontSize: '11px', fontWeight: 700, padding: '2px 8px', borderRadius: '99px', background: `${color}22`, color }}>
                      {getScoreLabel(merchant.totalScore)}
                    </span>
                    <span style={{ fontSize: '11px', fontWeight: 600, padding: '2px 8px', borderRadius: '99px', background: `${statusColor}22`, color: statusColor }}>
                      {merchant.status.toLowerCase().replace('_', ' ')}
                    </span>
                  </div>
                </div>
              </div>

              {/* Score breakdown */}
              <div style={{ padding: '12px 16px', borderBottom: '1px solid var(--color-border)' }}>
                <p style={{ fontSize: '11px', color: 'var(--color-text-3)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px', fontWeight: 600 }}>
                  Score Breakdown
                </p>
                {[
                  { label: 'Payment History', score: merchant.paymentScore, max: 40 },
                  { label: 'Credit Utilisation', score: merchant.creditScore, max: 30 },
                  { label: 'Order Activity', score: merchant.orderScore, max: 30 },
                ].map(item => (
                  <div key={item.label} style={{ marginBottom: '8px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '3px' }}>
                      <span style={{ fontSize: '11px', color: 'var(--color-text-2)' }}>{item.label}</span>
                      <span style={{ fontSize: '11px', fontFamily: 'var(--font-mono)', color: 'var(--color-text-2)' }}>{item.score}/{item.max}</span>
                    </div>
                    <div style={{ height: 5, background: 'var(--color-border)', borderRadius: '99px', overflow: 'hidden' }}>
                      <div style={{
                        height: '100%', borderRadius: '99px',
                        width: `${(item.score / item.max) * 100}%`,
                        background: getScoreColor((item.score / item.max) * 100),
                        transition: 'width 0.6s ease',
                      }} />
                    </div>
                  </div>
                ))}
              </div>

              {/* Stats */}
              <div style={{ padding: '12px 16px', display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '8px' }}>
                {[
                  { icon: <ShoppingCart size={12} />, label: 'Orders', value: merchant.orderCount },
                  { icon: <FileText size={12} />, label: 'Pending Inv.', value: merchant.pendingInvoices },
                  { icon: <CreditCard size={12} />, label: 'Credit Used', value: `${merchant.utilisation}%` },
                ].map(stat => (
                  <div key={stat.label} style={{ textAlign: 'center' }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '3px', color: 'var(--color-text-3)', marginBottom: '2px' }}>
                      {stat.icon}
                      <span style={{ fontSize: '10px', textTransform: 'uppercase', letterSpacing: '0.04em' }}>{stat.label}</span>
                    </div>
                    <p style={{ fontSize: '14px', fontWeight: 700, fontFamily: 'var(--font-mono)' }}>{stat.value}</p>
                  </div>
                ))}
              </div>
            </Card>
          );
        })}

        {scorecards.length === 0 && (
          <p style={{ fontSize: '13px', color: 'var(--color-text-3)', padding: '32px' }}>No merchant data available.</p>
        )}
      </div>
    </Page>
  );
}
