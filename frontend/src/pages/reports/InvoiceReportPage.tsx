/**
 * pages/reports/InvoiceReportPage.tsx
 * On-demand invoice report filterable by merchant and date range.
 * Fetches raw invoice data from reportsApi.invoices() and displays
 * invoice ID, dates, amounts, and payment status in a printable table.
 *
 * Merchant filter defaults to "all" to show invoices across the whole system.
 */
import React, { useState } from "react";
import { FileText, Printer } from "lucide-react";
import { Page } from "@/components/Layout/Header";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { Field, Select } from "@/components/ui/Modal";
import { useAppData } from "@/context/AppDataContext";
import { reportsApi } from "@/api/endpoints";
import type { ApiInvoice } from "@/api/types";
import type { TableColumn } from "@/types";
import { ReportError, Spinner, DateRange } from "@/pages/reports/ReportHelper";

export function InvoiceReportPage() {
  const { merchants } = useAppData();
  const [merchantId, setMerchantId] = useState("all");
  const [start, setStart] = useState("2026-01-01");
  const [end, setEnd] = useState("2026-12-31");
  const [data, setData] = useState<ApiInvoice[] | null>(null);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState("");

  const generate = async () => {
    setBusy(true);
    setErr("");
    setData(null);
    try {
      const result =
        merchantId === "all"
          ? await reportsApi.allInvoices(start, end)
          : await reportsApi.merchantInvoices(
              parseInt(merchantId, 10),
              start,
              end,
            );
      setData(result);
    } catch (e) {
      setErr(e instanceof Error ? e.message : String(e));
    } finally {
      setBusy(false);
    }
  };

  const total = data?.reduce((s, i) => s + Number(i.amountDue), 0) ?? 0;

  const cols: TableColumn<ApiInvoice>[] = [
    {
      key: "invoiceId",
      header: "Invoice",
      render: (r) => (
        <span className="mono" style={{ fontWeight: 700 }}>
          {r.invoiceId}
        </span>
      ),
    },
    {
      key: "account",
      header: "Merchant",
      render: (r) => <span>{r.account?.companyName ?? "—"}</span>,
    },
    {
      key: "invoiceDate",
      header: "Issued",
      render: (r) => (
        <span className="mono" style={{ fontSize: "12px" }}>
          {r.invoiceDate}
        </span>
      ),
    },
    {
      key: "amountDue",
      header: "Amount (£)",
      align: "right",
      render: (r) => (
        <span className="mono" style={{ fontWeight: 600 }}>
          £{Number(r.amountDue).toFixed(2)}
        </span>
      ),
    },
    {
      key: "order",
      header: "Status",
      render: (r) => {
        const paid = r.order?.paymentStatus === "PAID";
        return (
          <span
            style={{
              fontSize: "11px",
              fontWeight: 600,
              padding: "2px 8px",
              borderRadius: "99px",
              background: paid
                ? "var(--color-success-bg)"
                : "var(--color-warning-bg)",
              color: paid ? "#065f46" : "#92400e",
            }}
          >
            {paid ? "Paid" : "Pending"}
          </span>
        );
      },
    },
  ];

  return (
    <Page
      title="Invoice Reports"
      subtitle="Invoices for a specific merchant or all invoices for a given period"
      actions={
        data && (
          <Button
            variant="ghost"
            size="sm"
            icon={<Printer size={14} />}
            onClick={() => window.print()}
          >
            Print
          </Button>
        )
      }
    >
      <div style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
        <div
          style={{
            display: "flex",
            gap: "12px",
            alignItems: "flex-end",
            flexWrap: "wrap",
          }}
        >
          <Field label="Merchant">
            <Select
              value={merchantId}
              onChange={(e) => {
                setMerchantId(e.target.value);
                setData(null);
              }}
              style={{ width: "220px" }}
            >
              <option value="all">All Merchants</option>
              {merchants.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.companyName}
                </option>
              ))}
            </Select>
          </Field>
          <DateRange
            start={start}
            end={end}
            onStart={setStart}
            onEnd={setEnd}
          />
          <Button icon={<FileText size={14} />} onClick={generate}>
            Generate
          </Button>
        </div>
        {busy && <Spinner />}
        {err && <ReportError msg={err} />}
        {data && (
          <>
            {data.length === 0 ? (
              <p style={{ fontSize: "13px", color: "var(--color-text-3)" }}>
                No invoices in this period.
              </p>
            ) : (
              <Table columns={cols} data={data} keyField="invoiceId" />
            )}
            <div
              style={{
                display: "flex",
                justifyContent: "flex-end",
                padding: "10px 14px",
                background: "var(--color-surface-2)",
                borderRadius: "var(--radius-sm)",
              }}
            >
              <p style={{ fontSize: "13px" }}>
                Total Invoiced:{" "}
                <strong className="mono">£{total.toFixed(2)}</strong>
              </p>
            </div>
          </>
        )}
      </div>
    </Page>
  );
}
