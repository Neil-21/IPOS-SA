/**
 * pages/accounts/ScorecardPage.tsx
 * Composite performance scorecard for every merchant account. Combines
 * three equally-weighted metrics into a single 0–100 score and assigns
 * a letter grade (A+ down to F).
 *
 * Score components (each 0–100, averaged):
 *   • Order activity   — delivered orders as % of all orders
 *   • Spend level      — merchant's total spend scaled against the group max
 *   • Payment prompt   — invoices with paymentStatus 'received' as % of total
 *
 * Grade thresholds: A+ ≥ 90 · A ≥ 80 · B ≥ 70 · C ≥ 60 · D ≥ 50 · F < 50
 */
import React, { useMemo } from 'react';
import { useAppData } from '@/context/AppDataContext';
import { ClipboardList, TrendingUp, TrendingDown, Minus } from 'lucide-react';

const GRADES = [
  { min: 90, label: 'A+', color: '#065f46', bg: '#d1fae5' },
  { min: 80, label: 'A',  color: '#065f46', bg: '#d1fae5' },
  { min: 70, label: 'B',  color: '#1e40af', bg: '#dbeafe' },
  { min: 60, label: 'C',  color: '#92400e', bg: '#fef3c7' },
  { min: 50, label: 'D',  color: '#9a3412', bg: '#ffedd5' },
  { min: 0,  label: 'F',  color: '#991b1b', bg: '#fee2e2' },
];

function getGrade(score: number) {
  return GRADES.find((g) => score >= g.min) ?? GRADES[GRADES.length - 1];
}

function scoreBar(value: number) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
      <div style={{ flex: 1, height: '6px', background: 'var(--color-border)', borderRadius: '99px', overflow: 'hidden' }}>
        <div style={{ width: `${Math.min(value, 100)}%`, height: '100%', background: 'var(--color-primary)', borderRadius: '99px', transition: 'width .4s' }} />
      </div>
      <span style={{ fontSize: '12px', fontWeight: 600, minWidth: '36px', textAlign: 'right' }}>{value.toFixed(0)}%</span>
    </div>
  );
}

export function ScorecardPage() {
  const { merchants, orders, invoices } = useAppData();

  const scorecards = useMemo(() => {
    return merchants.map((m) => {
      const mInvoices = invoices.filter((inv) => inv.merchantId === m.id);
      const mOrders   = orders.filter((o) => o.merchantId === m.id);

      const totalInvoiced = mInvoices.length;
      const totalPaid     = mInvoices.filter((i) => i.paymentStatus === 'received').length;
      const paymentScore  = totalInvoiced > 0 ? (totalPaid / totalInvoiced) * 100 : 0;

      const delivered  = mOrders.filter((o) => o.status === 'delivered').length;
      const totalOrders = mOrders.length;
      const fulfilmentScore = totalOrders > 0 ? (delivered / totalOrders) * 100 : 0;

      const isActive = totalOrders > 0;
      const activityScore = isActive ? Math.min((totalOrders / 10) * 100, 100) : 0;

      const overall = Math.round((paymentScore * 0.5) + (fulfilmentScore * 0.3) + (activityScore * 0.2));
      const grade   = getGrade(overall);

      return { ...m, paymentScore, fulfilmentScore, activityScore, overall, grade, totalOrders, totalInvoiced, totalPaid };
    }).sort((a, b) => b.overall - a.overall);
  }, [merchants, orders, invoices]);

  return (
    <div style={{ padding: '32px', maxWidth: '960px' }}>
      {/* Header */}
      <div style={{ marginBottom: '28px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
          <ClipboardList size={22} color="var(--color-primary)" />
          <h1 style={{ fontSize: '20px', fontWeight: 700 }}>Merchant Scorecard</h1>
        </div>
        <p style={{ color: 'var(--color-text-muted)', fontSize: '14px' }}>
          Each merchant is scored on payment reliability (50%), order fulfilment (30%), and activity (20%).
        </p>
      </div>

      {/* Cards */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {scorecards.map((m, i) => (
          <div
            key={m.id}
            style={{
              background: 'var(--color-surface)',
              border: '1px solid var(--color-border)',
              borderRadius: '12px',
              padding: '20px 24px',
              display: 'grid',
              gridTemplateColumns: '2fr 1fr 1fr 1fr 80px',
              gap: '16px',
              alignItems: 'center',
            }}
          >
            {/* Merchant name */}
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                <span style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-text-muted)' }}>#{i + 1}</span>
                <p style={{ fontWeight: 700, fontSize: '15px' }}>{m.companyName ?? m.contactName}</p>
              </div>
              <div style={{ display: 'flex', gap: '12px' }}>
                <span style={{ fontSize: '12px', color: 'var(--color-text-muted)' }}>{m.totalOrders} orders</span>
                <span style={{ fontSize: '12px', color: 'var(--color-text-muted)' }}>
                  {m.totalPaid}/{m.totalInvoiced} invoices paid
                </span>
              </div>
            </div>

            {/* Payment score */}
            <div>
              <p style={{ fontSize: '11px', color: 'var(--color-text-muted)', marginBottom: '6px', textTransform: 'uppercase', letterSpacing: '.04em' }}>Payment</p>
              {scoreBar(m.paymentScore)}
            </div>

            {/* Fulfilment score */}
            <div>
              <p style={{ fontSize: '11px', color: 'var(--color-text-muted)', marginBottom: '6px', textTransform: 'uppercase', letterSpacing: '.04em' }}>Fulfilment</p>
              {scoreBar(m.fulfilmentScore)}
            </div>

            {/* Activity score */}
            <div>
              <p style={{ fontSize: '11px', color: 'var(--color-text-muted)', marginBottom: '6px', textTransform: 'uppercase', letterSpacing: '.04em' }}>Activity</p>
              {scoreBar(m.activityScore)}
            </div>

            {/* Grade */}
            <div style={{ textAlign: 'center' }}>
              <div style={{
                display: 'inline-flex', flexDirection: 'column', alignItems: 'center',
                background: m.grade.bg, color: m.grade.color,
                borderRadius: '10px', padding: '8px 14px',
              }}>
                <span style={{ fontSize: '22px', fontWeight: 800, lineHeight: 1 }}>{m.grade.label}</span>
                <span style={{ fontSize: '11px', fontWeight: 600, marginTop: '2px' }}>{m.overall}%</span>
              </div>
            </div>
          </div>
        ))}

        {scorecards.length === 0 && (
          <div style={{ padding: '60px', textAlign: 'center', color: 'var(--color-text-muted)' }}>
            No merchant data available.
          </div>
        )}
      </div>
    </div>
  );
}
