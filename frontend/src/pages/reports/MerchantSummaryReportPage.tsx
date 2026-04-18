/**
 * pages/reports/MerchantSummaryReportPage.tsx
 * On-demand summary report for a single merchant's orders over a date range.
 * Displays order count, total value, and status breakdown fetched from
 * reportsApi.merchantOrdersSummary().
 *
 * Flow: select merchant + dates → Generate → show summary cards + order table.
 */
import React, { useState } from "react";
import { FileText, Printer } from "lucide-react";
import { Page } from "@/components/Layout/Header";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { Field, Select } from "@/components/ui/Modal";
import { useAppData } from "@/context/AppDataContext";
import { reportsApi } from "@/api/endpoints";
import type { ApiMerchantOrdersSummary } from "@/api/types";
import type { TableColumn } from "@/types";
import { ReportError, Spinner, DateRange } from "@/pages/reports/ReportHelper";

export function MerchantSummaryReportPage() {
  const { merchants } = useAppData();
  const [merchantId, setMerchantId] = useState("");
  const [start, setStart] = useState("2026-01-01");
  const [end, setEnd] = useState("2026-12-31");
  const [data, setData] = useState<ApiMerchantOrdersSummary | null>(null);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState("");

  const generate = async () => {
    if (!merchantId) {
      setErr("Please select a merchant.");
      return;
    }
    setBusy(true);
    setErr("");
    setData(null);
    try {
      setData(
        await reportsApi.merchantSummary(parseInt(merchantId, 10), start, end),
      );
    } catch (e) {
      setErr(e instanceof Error ? e.message : String(e));
    } finally {
      setBusy(false);
    }
  };

  type Row = ApiMerchantOrdersSummary["orders"][0];
  const cols: TableColumn<Row>[] = [
    {
      key: "orderId",
      header: "Order ID",
      render: (r) => (
        <span className="mono" style={{ fontWeight: 700 }}>
          {r.orderId}
        </span>
      ),
    },
    {
      key: "orderDate",
      header: "Ordered",
      render: (r) => (
        <span className="mono" style={{ fontSize: "12px" }}>
          {r.orderDate}
        </span>
      ),
    },
    {
      key: "totalValue",
      header: "Amount (£)",
      align: "right",
      render: (r) => (
        <span className="mono">£{Number(r.totalValue).toFixed(2)}</span>
      ),
    },
    {
      key: "dispatchDate",
      header: "Dispatched",
      render: (r) => (
        <span className="mono" style={{ fontSize: "12px" }}>
          {r.dispatchDate ?? "Pending"}
        </span>
      ),
    },
    {
      key: "deliveryDate",
      header: "Delivered",
      render: (r) => (
        <span className="mono" style={{ fontSize: "12px" }}>
          {r.deliveryDate ?? "Pending"}
        </span>
      ),
    },
    {
      key: "paymentStatus",
      header: "Paid",
      render: (r) => (
        <span
          style={{
            fontSize: "11px",
            fontWeight: 600,
            padding: "2px 8px",
            borderRadius: "99px",
            background:
              r.paymentStatus === "PAID"
                ? "var(--color-success-bg)"
                : "var(--color-warning-bg)",
            color: r.paymentStatus === "PAID" ? "#065f46" : "#92400e",
          }}
        >
          {r.paymentStatus === "PAID" ? "Paid" : "Pending"}
        </span>
      ),
    },
  ];

  return (
    <Page
      title="Merchant Orders Summary"
      subtitle="Appendix 4 — Order ID, date, value, dispatch and delivery dates per merchant"
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
              style={{ width: "260px" }}
            >
              <option value="">— Select merchant —</option>
              {merchants.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.companyName} ({m.iposAccount})
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
            <div
              style={{
                padding: "12px 14px",
                background: "var(--color-surface-2)",
                borderRadius: "var(--radius-sm)",
                fontSize: "13px",
              }}
            >
              <p style={{ fontWeight: 700 }}>{data.companyName}</p>
              <p
                style={{
                  color: "var(--color-text-3)",
                  fontSize: "11px",
                  marginTop: "4px",
                }}
              >
                Period: {start} to {end}
              </p>
            </div>
            {data.orders.length === 0 ? (
              <p style={{ fontSize: "13px", color: "var(--color-text-3)" }}>
                No orders in this period.
              </p>
            ) : (
              <Table columns={cols} data={data.orders} keyField="orderId" />
            )}
            <div
              style={{
                display: "flex",
                justifyContent: "flex-end",
                gap: "32px",
                padding: "10px 14px",
                background: "var(--color-surface-2)",
                borderRadius: "var(--radius-sm)",
              }}
            >
              <p style={{ fontSize: "13px" }}>
                Total Orders:{" "}
                <strong className="mono">{data.orders.length}</strong>
              </p>
              <p style={{ fontSize: "13px" }}>
                Total Value:{" "}
                <strong className="mono">
                  £{Number(data.totalValue).toFixed(2)}
                </strong>
              </p>
            </div>
            <p
              style={{
                fontSize: "11px",
                color: "var(--color-text-3)",
                textAlign: "right",
              }}
            >
              Generated: {new Date().toLocaleDateString("en-GB")}
            </p>
          </>
        )}
      </div>
    </Page>
  );
}
