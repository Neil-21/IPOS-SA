import { useMemo } from 'react';
import {
  PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid
} from 'recharts';
import { Page } from '@/components/Layout/Header';
import { Card } from '@/components/ui/Card';
import { useAppData } from '@/context/AppDataContext';

const COLORS = ['#4f98a3', '#e8af34', '#6daa45', '#a13544', '#7a39bb', '#da7101', '#006494', '#d19900'];

export default function AnalyticsPage() {
  const { orders, invoices, merchants } = useAppData();

  // Revenue by Merchant
  const revenueByMerchant = useMemo(() => {
    const map: Record<string, number> = {};
    orders.forEach(o => {
      const name = o.merchantName ?? `Merchant ${o.accountId}`;
      map[name] = (map[name] ?? 0) + o.totalAmount;
    });
    return Object.entries(map)
      .map(([name, value]) => ({ name, value: parseFloat(value.toFixed(2)) }))
      .sort((a, b) => b.value - a.value);
  }, [orders]);

  // Order Status Breakdown
  const orderStatusData = useMemo(() => {
    const STATUS_LABELS: Record<string, string> = {
      ACCEPTED: 'Accepted',
      BEING_PROCESSED: 'Processing',
      DISPATCHED: 'Dispatched',
      DELIVERED: 'Delivered',
    };
    const map: Record<string, number> = {};
    orders.forEach(o => {
      const label = STATUS_LABELS[o.orderStatus] ?? o.orderStatus;
      map[label] = (map[label] ?? 0) + 1;
    });
    return Object.entries(map).map(([name, value]) => ({ name, value }));
  }, [orders]);

  // Payment Health by Merchant
  const paymentHealth = useMemo(() => {
    const map: Record<string, { paid: number; partial: number; pending: number }> = {};
    merchants.forEach(m => {
      map[m.companyName] = { paid: 0, partial: 0, pending: 0 };
    });
    invoices.forEach(inv => {
      const merchant = merchants.find(m => m.id === inv.merchantId);
      const name = merchant?.companyName ?? `Merchant ${inv.merchantId}`;
      if (!map[name]) map[name] = { paid: 0, partial: 0, pending: 0 };
      if (inv.paymentStatus === 'PAID') map[name].paid++;
      else if (inv.paymentStatus === 'PARTIAL') map[name].partial++;
      else map[name].pending++;
    });
    return Object.entries(map).map(([name, counts]) => ({ name, ...counts }));
  }, [invoices, merchants]);

  const totalRevenue = revenueByMerchant.reduce((s, d) => s + d.value, 0);

  return (
    <Page title="Business Analytics">

      {/* Summary strip */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px', marginBottom: '24px' }}>
        {[
          { label: 'Total Revenue', value: `£${totalRevenue.toLocaleString('en-GB', { minimumFractionDigits: 2 })}` },
          { label: 'Total Orders', value: String(orders.length) },
          { label: 'Active Merchants', value: String(merchants.filter(m => m.accountStatus === 'NORMAL').length) },
        ].map(stat => (
          <Card key={stat.label} padding="0">
            <div style={{ padding: '16px 20px' }}>
              <p style={{ fontSize: '11px', color: 'var(--color-text-3)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
                {stat.label}
              </p>
              <p style={{ fontSize: '24px', fontWeight: 700, fontFamily: 'var(--font-mono)', marginTop: '4px' }}>
                {stat.value}
              </p>
            </div>
          </Card>
        ))}
      </div>

      {/* Two pie charts */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>

        <Card padding="0">
          <div style={{ padding: '14px 16px', borderBottom: '1px solid var(--color-border)' }}>
            <p style={{ fontWeight: 700, fontSize: '13px' }}>Revenue by Merchant</p>
            <p style={{ fontSize: '11px', color: 'var(--color-text-3)', marginTop: '2px' }}>Share of total order value</p>
          </div>
          {revenueByMerchant.length === 0 ? (
            <p style={{ padding: '16px', fontSize: '12px', color: 'var(--color-text-3)' }}>No data available.</p>
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie data={revenueByMerchant} cx="50%" cy="50%" innerRadius={60} outerRadius={95} paddingAngle={3} dataKey="value">
                  {revenueByMerchant.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(v: number) => [`£${v.toFixed(2)}`, 'Revenue']} contentStyle={{ borderRadius: '8px', fontSize: '12px' }} />
                <Legend iconType="circle" iconSize={8} formatter={(v) => <span style={{ fontSize: '11px', color: 'var(--color-text-2)' }}>{v}</span>} />
              </PieChart>
            </ResponsiveContainer>
          )}
        </Card>

        <Card padding="0">
          <div style={{ padding: '14px 16px', borderBottom: '1px solid var(--color-border)' }}>
            <p style={{ fontWeight: 700, fontSize: '13px' }}>Orders by Status</p>
            <p style={{ fontSize: '11px', color: 'var(--color-text-3)', marginTop: '2px' }}>Breakdown of all order statuses</p>
          </div>
          {orderStatusData.length === 0 ? (
            <p style={{ padding: '16px', fontSize: '12px', color: 'var(--color-text-3)' }}>No data available.</p>
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie data={orderStatusData} cx="50%" cy="50%" innerRadius={60} outerRadius={95} paddingAngle={3} dataKey="value">
                  {orderStatusData.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(v: number) => [v, 'Orders']} contentStyle={{ borderRadius: '8px', fontSize: '12px' }} />
                <Legend iconType="circle" iconSize={8} formatter={(v) => <span style={{ fontSize: '11px', color: 'var(--color-text-2)' }}>{v}</span>} />
              </PieChart>
            </ResponsiveContainer>
          )}
        </Card>
      </div>

      {/* Bar chart */}
      <Card padding="0">
        <div style={{ padding: '14px 16px', borderBottom: '1px solid var(--color-border)' }}>
          <p style={{ fontWeight: 700, fontSize: '13px' }}>Invoice Payment Health by Merchant</p>
          <p style={{ fontSize: '11px', color: 'var(--color-text-3)', marginTop: '2px' }}>Paid vs Partial vs Pending invoices</p>
        </div>
        {paymentHealth.length === 0 ? (
          <p style={{ padding: '16px', fontSize: '12px', color: 'var(--color-text-3)' }}>No invoice data available.</p>
        ) : (
          <div style={{ padding: '16px' }}>
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={paymentHealth} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
                <XAxis dataKey="name" tick={{ fontSize: 11, fill: 'var(--color-text-2)' }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: 'var(--color-text-2)' }} />
                <Tooltip contentStyle={{ borderRadius: '8px', fontSize: '12px' }} />
                <Legend iconType="circle" iconSize={8} formatter={(v) => <span style={{ fontSize: '11px', color: 'var(--color-text-2)' }}>{v}</span>} />
                <Bar dataKey="paid" name="Paid" fill="#6daa45" radius={[4, 4, 0, 0]} />
                <Bar dataKey="partial" name="Partial" fill="#e8af34" radius={[4, 4, 0, 0]} />
                <Bar dataKey="pending" name="Pending" fill="#a13544" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </Card>

    </Page>
  );
}
