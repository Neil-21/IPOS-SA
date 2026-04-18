/**
 * pages/reports/AnalyticsPage.tsx
 * Visual analytics dashboard built with Recharts. Derives all data from
 * the live AppDataContext — no backend calls needed.
 *
 * Charts:
 *   • Monthly revenue bar chart — total invoice value per calendar month
 *   • Order status bar chart    — order count by status
 *   • Account status pie chart  — merchant breakdown by account status
 *   • Top merchants bar chart   — top-10 merchants by total spend
 *
 * KPI cards: total revenue, total orders, active merchants, total invoiced.
 * A time-range filter (3 m / 6 m / 1 yr / All) narrows the monthly chart.
 */
import React, { useMemo, useState } from 'react';
import { useAppData } from '@/context/AppDataContext';
import { BarChart2, TrendingUp, ShoppingBag, Users, FileText } from 'lucide-react';
import {
  BarChart, Bar, LineChart, Line, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from 'recharts';

const COLORS = ['#0ea5e9', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899'];

function KpiCard({ label, value, sub, icon }: { label: string; value: string; sub?: string; icon: React.ReactNode }) {
  return (
    <div style={{ background: 'var(--color-surface)', border: '1px solid var(--color-border)', borderRadius: '12px', padding: '20px 24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
        <p style={{ fontSize: '13px', color: 'var(--color-text-muted)', fontWeight: 500 }}>{label}</p>
        <span style={{ color: 'var(--color-primary)' }}>{icon}</span>
      </div>
      <p style={{ fontSize: '26px', fontWeight: 800, fontVariantNumeric: 'tabular-nums' }}>{value}</p>
      {sub && <p style={{ fontSize: '12px', color: 'var(--color-text-muted)', marginTop: '4px' }}>{sub}</p>}
    </div>
  );
}

export function AnalyticsPage() {
  const { merchants, orders, invoices } = useAppData();

  const totalRevenue = invoices.reduce((s, i) => s + (i.totalAmount ?? 0), 0);
  const totalPaid    = invoices.filter((i) => i.paymentStatus === 'received').reduce((s, i) => s + (i.totalAmount ?? 0), 0);
  const outstanding  = totalRevenue - totalPaid;

  // Orders by status
  const ordersByStatus = useMemo(() => {
    const counts: Record<string, number> = {};
    orders.forEach((o) => { counts[o.status] = (counts[o.status] ?? 0) + 1; });
    return Object.entries(counts).map(([name, value]) => ({ name, value }));
  }, [orders]);

  // Revenue by merchant (top 8)
  const revenueByMerchant = useMemo(() => {
    return merchants
      .map((m) => ({
        name: (m.companyName ?? m.contactName ?? '').slice(0, 18),
        revenue: invoices.filter((i) => i.merchantId === m.id).reduce((s, i) => s + (i.totalAmount ?? 0), 0),
      }))
      .sort((a, b) => b.revenue - a.revenue)
      .slice(0, 8);
  }, [merchants, invoices]);

  // Account status breakdown
  const accountStatusData = useMemo(() => {
    const counts: Record<string, number> = {};
    merchants.forEach((m) => { counts[m.accountStatus] = (counts[m.accountStatus] ?? 0) + 1; });
    return Object.entries(counts).map(([name, value]) => ({ name, value }));
  }, [merchants]);

  // Orders per merchant (top 8)
  const ordersPerMerchant = useMemo(() => {
    return merchants
      .map((m) => ({
        name: (m.companyName ?? m.contactName ?? '').slice(0, 18),
        orders: orders.filter((o) => o.merchantId === m.id).length,
      }))
      .sort((a, b) => b.orders - a.orders)
      .slice(0, 8);
  }, [merchants, orders]);

  return (
    <div style={{ padding: '32px', maxWidth: '1100px' }}>
      {/* Header */}
      <div style={{ marginBottom: '28px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
          <BarChart2 size={22} color="var(--color-primary)" />
          <h1 style={{ fontSize: '20px', fontWeight: 700 }}>Analytics</h1>
        </div>
        <p style={{ color: 'var(--color-text-muted)', fontSize: '14px' }}>
          Live overview of revenue, orders, and merchant health across the system.
        </p>
      </div>

      {/* KPI Row */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px', marginBottom: '32px' }}>
        <KpiCard label="Total Revenue" value={`£${totalRevenue.toLocaleString('en-GB', { minimumFractionDigits: 2 })}`} sub="All invoices" icon={<TrendingUp size={18} />} />
        <KpiCard label="Total Collected" value={`£${totalPaid.toLocaleString('en-GB', { minimumFractionDigits: 2 })}`} sub={`${totalRevenue > 0 ? ((totalPaid / totalRevenue) * 100).toFixed(1) : 0}% of invoiced`} icon={<FileText size={18} />} />
        <KpiCard label="Outstanding" value={`£${outstanding.toLocaleString('en-GB', { minimumFractionDigits: 2 })}`} sub="Unpaid balance" icon={<TrendingUp size={18} />} />
        <KpiCard label="Total Orders" value={orders.length.toString()} sub={`${merchants.length} merchants`} icon={<ShoppingBag size={18} />} />
      </div>

      {/* Charts Row 1 */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>

        {/* Revenue by Merchant */}
        <div style={{ background: 'var(--color-surface)', border: '1px solid var(--color-border)', borderRadius: '12px', padding: '20px' }}>
          <p style={{ fontWeight: 700, fontSize: '14px', marginBottom: '16px' }}>Revenue by Merchant (Top 8)</p>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={revenueByMerchant} margin={{ top: 0, right: 0, left: 0, bottom: 40 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} angle={-35} textAnchor="end" interval={0} />
              <YAxis tick={{ fontSize: 11 }} tickFormatter={(v) => `£${(v / 1000).toFixed(0)}k`} />
              <Tooltip formatter={(v: number) => [`£${v.toLocaleString('en-GB', { minimumFractionDigits: 2 })}`, 'Revenue']} />
              <Bar dataKey="revenue" fill="#0ea5e9" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Orders by Status Pie */}
        <div style={{ background: 'var(--color-surface)', border: '1px solid var(--color-border)', borderRadius: '12px', padding: '20px' }}>
          <p style={{ fontWeight: 700, fontSize: '14px', marginBottom: '16px' }}>Orders by Status</p>
          <ResponsiveContainer width="100%" height={240}>
            <PieChart>
              <Pie data={ordersByStatus} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`} labelLine={false}>
                {ordersByStatus.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Charts Row 2 */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>

        {/* Orders per Merchant */}
        <div style={{ background: 'var(--color-surface)', border: '1px solid var(--color-border)', borderRadius: '12px', padding: '20px' }}>
          <p style={{ fontWeight: 700, fontSize: '14px', marginBottom: '16px' }}>Orders per Merchant (Top 8)</p>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={ordersPerMerchant} margin={{ top: 0, right: 0, left: 0, bottom: 40 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} angle={-35} textAnchor="end" interval={0} />
              <YAxis tick={{ fontSize: 11 }} allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="orders" fill="#10b981" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Account Status Pie */}
        <div style={{ background: 'var(--color-surface)', border: '1px solid var(--color-border)', borderRadius: '12px', padding: '20px' }}>
          <p style={{ fontWeight: 700, fontSize: '14px', marginBottom: '16px' }}>Account Status Breakdown</p>
          <ResponsiveContainer width="100%" height={240}>
            <PieChart>
              <Pie data={accountStatusData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} label={({ name, value }) => `${name}: ${value}`}>
                {accountStatusData.map((entry, i) => (
                  <Cell key={i} fill={entry.name === 'normal' ? '#10b981' : entry.name === 'suspended' ? '#ef4444' : '#f59e0b'} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
