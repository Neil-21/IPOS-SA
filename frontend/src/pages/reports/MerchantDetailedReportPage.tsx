/**
 * pages/reports/MerchantDetailedReportPage.tsx
 * On-demand detailed order report for a single merchant. Shows every order
 * within the selected date range with full line-item breakdowns, fetched
 * from reportsApi.merchantDetailedOrders().
 *
 * Flow: select merchant + dates → Generate → expandable order rows with items.
 */
import React, { useState } from "react";
import { FileText, Printer } from "lucide-react";
import { Page } from "@/components/Layout/Header";
import { Card } from "@/components/ui/Card";
import { Table } from "@/components/ui/Table";
import { Button } from "@/components/ui/Button";
import { Field, Select } from "@/components/ui/Modal";
import { useAppData } from "@/context/AppDataContext";
import { reportsApi } from "@/api/endpoints";
import type { ApiDetailedOrderReport } from "@/api/types";
import type { TableColumn } from "@/types";
import { ReportError, Spinner, DateRange } from "@/pages/reports/ReportHelper";

export function MerchantDetailedReportPage() {
  const { merchants } = useAppData();
  const [merchantId, setMerchantId] = useState("");
  const [start, setStart] = useState("2026-01-01");
  const [end, setEnd] = useState("2026-12-31");
  const [data, setData] = useState<ApiDetailedOrderReport | null>(null);
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
        await reportsApi.merchantDetailed(parseInt(merchantId, 10), start, end),
      );
    } catch (e) {
      setErr(e instanceof Error ? e.message : String(e));
    } finally {
      setBusy(false);
    }
  };

  type ItemRow = ApiDetailedOrderReport["orders"][0]["items"][0];
  const itemCols: TableColumn<ItemRow>[] = [
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
      key: "quantity",
      header: "Qty",
      align: "right",
      render: (r) => <span className="mono">{r.quantity}</span>,
    },
    {
      key: "unitCost",
      header: "Unit (£)",
      align: "right",
      render: (r) => (
        <span className="mono">£{Number(r.unitCost).toFixed(2)}</span>
      ),
    },
    {
      key: "totalCost",
      header: "Amount",
      align: "right",
      render: (r) => (
        <strong className="mono">£{Number(r.totalCost).toFixed(2)}</strong>
      ),
    },
  ];

  return (
    <Page
      title="Merchant Orders Detailed"
      subtitle="Appendix 5 — Full activity with individual items, quantities, costs, discounts per order"
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
          <div
            style={{ display: "flex", flexDirection: "column", gap: "14px" }}
          >
            <div
              style={{
                padding: "14px 16px",
                background: "var(--color-surface-2)",
                borderRadius: "var(--radius-sm)",
                fontSize: "13px",
              }}
            >
              <p style={{ fontWeight: 700, fontSize: "14px" }}>
                {data.companyName}
              </p>
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
              data.orders.map((order) => (
                <Card key={order.orderId} padding="0">
                  <div
                    style={{
                      padding: "12px 14px",
                      background: "var(--color-surface-2)",
                      display: "flex",
                      justifyContent: "space-between",
                    }}
                  >
                    <div>
                      <span className="mono" style={{ fontWeight: 700 }}>
                        {order.orderId}
                      </span>
                      <span
                        style={{
                          fontSize: "12px",
                          color: "var(--color-text-2)",
                          marginLeft: "12px",
                        }}
                      >
                        Ordered: {order.orderDate}
                      </span>
                    </div>
                    <div
                      style={{ display: "flex", gap: "16px", fontSize: "12px" }}
                    >
                      <span>
                        Total:{" "}
                        <strong className="mono">
                          £{Number(order.totalValue).toFixed(2)}
                        </strong>
                      </span>
                      {Number(order.discountApplied) > 0 && (
                        <span style={{ color: "var(--color-success)" }}>
                          Discount: −£{Number(order.discountApplied).toFixed(2)}
                        </span>
                      )}
                    </div>
                  </div>
                  <Table
                    columns={itemCols}
                    data={order.items}
                    keyField="itemId"
                  />
                </Card>
              ))
            )}
            <div
              style={{
                display: "flex",
                justifyContent: "flex-end",
                gap: "32px",
                padding: "12px 14px",
                background: "var(--color-surface-2)",
                borderRadius: "var(--radius-sm)",
              }}
            >
              <p style={{ fontSize: "13px" }}>
                Total Orders:{" "}
                <strong className="mono">{data.orders.length}</strong>
              </p>
              <p style={{ fontSize: "13px" }}>
                Grand Total:{" "}
                <strong className="mono">
                  £{Number(data.grandTotal).toFixed(2)}
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
              Generated: {new Date().toLocaleDateString("en-GB")} · By: Director
              of Operations
            </p>
          </div>
        )}
      </div>
    </Page>
  );
}
