/**
 * pages/reports/ReportHelper.tsx
 * Shared UI primitives used across all report pages:
 *   • ReportError  — styled inline error banner with an alert icon
 *   • Spinner      — animated "Generating report…" indicator
 *   • DateRange    — two date inputs (start / end) for report period selection
 *
 * Kept in one file so every report page imports from a single location.
 */
import React from 'react';
import { AlertCircle, Loader } from 'lucide-react';
import { Field, Input } from '@/components/ui/Modal';

export function ReportError({ msg }: { msg: string }) {
    return (
        <div style={{ padding: '12px 14px', background: 'var(--color-danger-bg)', border: '1px solid #fca5a5',
            borderRadius: 'var(--radius-sm)', fontSize: '13px', color: '#991b1b', display: 'flex', gap: '8px', alignItems: 'center' }}>
            <AlertCircle size={14} /> {msg}
        </div>
    );
}

export function Spinner() {
    return (
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: 'var(--color-text-3)' }}>
            <Loader size={14} style={{ animation: 'spin 1s linear infinite' }} />
            Generating report…
            <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
        </div>
    );
}

export function DateRange({ start, end, onStart, onEnd }: {
    start: string; end: string; onStart: (v: string) => void; onEnd: (v: string) => void;
}) {
    return (
        <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-end' }}>
            <Field label="From"><Input type="date" value={start} onChange={e => onStart(e.target.value)} style={{ width: '160px' }} /></Field>
            <Field label="To">  <Input type="date" value={end}   onChange={e => onEnd(e.target.value)}   style={{ width: '160px' }} /></Field>
        </div>
    );
}
