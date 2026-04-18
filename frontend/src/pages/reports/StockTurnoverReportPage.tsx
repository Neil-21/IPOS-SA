/**
 * pages/reports/StockTurnoverReportPage.tsx
 * On-demand stock turnover report showing opening stock, units sold,
 * units received, and closing stock per catalogue item for a date range.
 * Data is fetched from reportsApi.stockTurnover() and displayed in a
 * printable table.
 */
import React, { useState } from "react";
import { BarChart2, Printer } from "lucide-react";
import { Page } from "@/components/Layout/Header";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { reportsApi } from "@/api/endpoints";
import type { ApiStockTurnoverReport } from "@/api/types";
import type { TableColumn } from "@/types";
import { ReportError, Spinner, DateRange } from "@/pages/reports/ReportHelper";

export function StockTurnoverReportPage() {
  const [start, setStart] = useState("2026-01-01");
  const [end, setEnd] = useState("2026-12-31");
  const [data, setData] = useState<ApiStockTurnoverReport | null>(null);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState("");

  const generate = async () => {
    setBusy(true);
    setErr("");
    setData(null);
    try {
      setData(await reportsApi.stockTurnover(start, end));
    } catch (e) {
      setErr(e instanceof Error ? e.message : String(e));
    } finally {
      setBusy(false);
    }
  };

  type Row = ApiStockTurnoverReport["rows"][0];
  const cols: TableColumn<Row>[] = [
    {
      key: "itemId",
      header: "Item ID",
      render: (r) => (
        <span className="mono" style={{ fontSize: "12px" }}>
          {r.itemId}
        </span>
      ),
    },
    { key: "description", header: "Description" },
    {
      key: "quantityDelivered",
      header: "Received",
      align: "right",
      render: (r) => (
        <span
          className="mono"
          style={{ color: "var(--color-success)", fontWeight: 600 }}
        >
          +{r.quantityDelivered}
        </span>
      ),
    },
    {
      key: "quantitySold",
      header: "Sold",
      align: "right",
      render: (r) => (
        <span
          className="mono"
          style={{ color: "var(--color-danger)", fontWeight: 600 }}
        >
          −{r.quantitySold}
        </span>
      ),
    },
    {
      key: "netChange",
      header: "Net",
      align: "right",
      render: (r) => (
        <strong
          className="mono"
          style={{
            color:
              r.netChange >= 0 ? "var(--color-success)" : "var(--color-danger)",
          }}
        >
          {r.netChange >= 0 ? `+${r.netChange}` : r.netChange}
        </strong>
      ),
    },
  ];

  return (
    <Page
      title="Stock Turnover Report"
      subtitle="Stock received vs goods sold within a given period"
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
            alignItems: "flex-end",
            gap: "12px",
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
        {data &&
          (data.rows.length === 0 ? (
            <p style={{ fontSize: "13px", color: "var(--color-text-3)" }}>
              No stock movements in this period.
            </p>
          ) : (
            <Table columns={cols} data={data.rows} keyField="itemId" />
          ))}
      </div>
    </Page>
  );
}
