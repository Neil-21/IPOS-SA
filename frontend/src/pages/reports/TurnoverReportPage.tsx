/**
 * pages/reports/TurnoverReportPage.tsx
 * On-demand product turnover report fetched from the backend.
 * Shows units sold and revenue per catalogue item for a chosen date range.
 *
 * Flow: user sets start/end dates → clicks Generate → calls
 * reportsApi.turnover() → displays the result table.
 * The report is also printable via window.print().
 */
import React, { useState } from "react";
import { BarChart2, Printer } from "lucide-react";
import { Page } from "@/components/Layout/Header";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { Field, Input } from "@/components/ui/Modal";
import { reportsApi } from "@/api/endpoints";
import type { ApiTurnoverReport } from "@/api/types";
import type { TableColumn } from "@/types";
import { ReportError, Spinner, DateRange } from "@/pages/reports/ReportHelper";

export function TurnoverReportPage() {
  const [start, setStart] = useState("2026-01-01");
  const [end, setEnd] = useState("2026-12-31");
  const [data, setData] = useState<ApiTurnoverReport | null>(null);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState("");

  const generate = async () => {
    setBusy(true);
    setErr("");
    setData(null);
    try {
      setData(await reportsApi.turnover(start, end));
    } catch (e) {
      setErr(e instanceof Error ? e.message : String(e));
    } finally {
      setBusy(false);
    }
  };

  type Row = ApiTurnoverReport["rows"][0];
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
    { key: "companyName", header: "Merchant" },
    {
      key: "orderDate",
      header: "Date",
      render: (r) => (
        <span className="mono" style={{ fontSize: "12px" }}>
          {r.orderDate}
        </span>
      ),
    },
    {
      key: "totalValue",
      header: "Amount",
      align: "right",
      render: (r) => (
        <strong className="mono">£{Number(r.totalValue).toFixed(2)}</strong>
      ),
    },
  ];

  return (
    <Page
      title="Turnover Report"
      subtitle="Quantities sold and revenue received by InfoPharma for a given period"
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
          <DateRange
            start={start}
            end={end}
            onStart={setStart}
            onEnd={setEnd}
          />
          <Button icon={<BarChart2 size={14} />} onClick={generate}>
            Generate
          </Button>
        </div>
        {busy && <Spinner />}
        {err && <ReportError msg={err} />}
        {data && (
          <>
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "1fr 1fr",
                gap: "12px",
              }}
            >
              <div
                style={{
                  padding: "14px",
                  background: "var(--color-primary-bg)",
                  borderRadius: "var(--radius-sm)",
                }}
              >
                <p
                  style={{
                    fontSize: "11px",
                    color: "var(--color-primary-dk)",
                    fontWeight: 600,
                    textTransform: "uppercase",
                  }}
                >
                  Total Orders
                </p>
                <p
                  className="mono"
                  style={{
                    fontSize: "24px",
                    fontWeight: 700,
                    color: "var(--color-primary-dk)",
                    marginTop: "4px",
                  }}
                >
                  {data.totalOrders}
                </p>
              </div>
              <div
                style={{
                  padding: "14px",
                  background: "var(--color-success-bg)",
                  borderRadius: "var(--radius-sm)",
                }}
              >
                <p
                  style={{
                    fontSize: "11px",
                    color: "#065f46",
                    fontWeight: 600,
                    textTransform: "uppercase",
                  }}
                >
                  Total Revenue
                </p>
                <p
                  className="mono"
                  style={{
                    fontSize: "24px",
                    fontWeight: 700,
                    color: "#065f46",
                    marginTop: "4px",
                  }}
                >
                  £{Number(data.totalRevenue).toFixed(2)}
                </p>
              </div>
            </div>
            {data.rows.length === 0 ? (
              <p style={{ fontSize: "13px", color: "var(--color-text-3)" }}>
                No orders in this period.
              </p>
            ) : (
              <Table columns={cols} data={data.rows} keyField="orderId" />
            )}
            <p
              style={{
                fontSize: "11px",
                color: "var(--color-text-3)",
                textAlign: "right",
              }}
            >
              Generated: {new Date().toLocaleDateString("en-GB")} · Period:{" "}
              {start} to {end}
            </p>
          </>
        )}
      </div>
    </Page>
  );
}
