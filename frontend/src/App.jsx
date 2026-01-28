import { useMemo, useState } from "react";
import { login, getCustomers, getAccounts, createTransfer } from "./api";

function App() {
  const [token, setToken] = useState(null);

  const [customers, setCustomers] = useState([]);
  const [accounts, setAccounts] = useState([]);

  const [customerId, setCustomerId] = useState(1);

  const [fromAccountId, setFromAccountId] = useState(2);
  const [toAccountId, setToAccountId] = useState(1);
  const [amount, setAmount] = useState(1);

  const [transferResult, setTransferResult] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const balances = useMemo(() => {
    const byId = {};
    for (const a of accounts) byId[a.id] = a;
    return byId;
  }, [accounts]);

  async function handleLogin() {
    try {
      setError("");
      setLoading(true);
      const data = await login("admin", "password");
      setToken(data.token);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function loadCustomersClick() {
    try {
      setError("");
      setLoading(true);
      const data = await getCustomers(token);
      setCustomers(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function loadAccountsClick() {
    try {
      setError("");
      setLoading(true);
      const data = await getAccounts(token, customerId);
      setAccounts(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function doTransferClick() {
    try {
      setError("");
      setTransferResult(null);

      // tiny frontend guardrails
      if (!fromAccountId || !toAccountId) throw new Error("Account IDs required");
      if (fromAccountId === toAccountId)
        throw new Error("fromAccountId and toAccountId cannot be same");
      if (!amount || amount <= 0) throw new Error("Amount must be > 0");

      setLoading(true);

      const result = await createTransfer(token, fromAccountId, toAccountId, amount);
      setTransferResult(result);

      // refresh accounts after transfer attempt (both success & failed)
      const updatedAccounts = await getAccounts(token, customerId);
      setAccounts(updatedAccounts);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  const status = transferResult?.status;

  return (
    <div style={{ padding: 20, maxWidth: 900, margin: "0 auto", fontFamily: "system-ui, sans-serif" }}>
      <h1>BFSI Frontend</h1>

      {!token ? (
        <button onClick={handleLogin} disabled={loading}>
          {loading ? "Logging in..." : "Login"}
        </button>
      ) : (
        <>
          <p>
            JWT loaded ✅ <span style={{ opacity: 0.6 }}>(len: {token.length})</span>
          </p>

          <div
            style={{
              display: "flex",
              gap: 12,
              flexWrap: "wrap",
              alignItems: "center",
              marginBottom: 14,
            }}
          >
            <button onClick={loadCustomersClick} disabled={loading}>
              Load Customers
            </button>

            <label style={{ display: "flex", gap: 8, alignItems: "center" }}>
              Customer ID:
              <input
                type="number"
                value={customerId}
                onChange={(e) => setCustomerId(Number(e.target.value))}
                style={{ width: 90 }}
              />
            </label>

            <button onClick={loadAccountsClick} disabled={loading}>
              Load Accounts
            </button>
          </div>

          <div
            style={{
              border: "1px solid #ddd",
              padding: 12,
              borderRadius: 8,
              marginBottom: 14,
            }}
          >
            <h3 style={{ marginTop: 0 }}>Transfer</h3>

            <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
              <label style={{ display: "flex", gap: 8, alignItems: "center" }}>
                From Account:
                <input
                  type="number"
                  value={fromAccountId}
                  onChange={(e) => setFromAccountId(Number(e.target.value))}
                  style={{ width: 90 }}
                />
              </label>

              <label style={{ display: "flex", gap: 8, alignItems: "center" }}>
                To Account:
                <input
                  type="number"
                  value={toAccountId}
                  onChange={(e) => setToAccountId(Number(e.target.value))}
                  style={{ width: 90 }}
                />
              </label>

              <label style={{ display: "flex", gap: 8, alignItems: "center" }}>
                Amount:
                <input
                  type="number"
                  value={amount}
                  onChange={(e) => setAmount(Number(e.target.value))}
                  style={{ width: 110 }}
                />
              </label>

              <button onClick={doTransferClick} disabled={loading}>
                {loading ? "Processing..." : "Create Transfer"}
              </button>
            </div>

            {accounts.length > 0 && (
              <div style={{ marginTop: 10, fontSize: 14, opacity: 0.9 }}>
                <div>
                  From balance: <b>{balances[fromAccountId]?.balance ?? "?"}</b>
                </div>
                <div>
                  To balance: <b>{balances[toAccountId]?.balance ?? "?"}</b>
                </div>
              </div>
            )}


            {transferResult && (
              <div
                style={{
                  marginTop: 12,
                  padding: 12,
                  borderRadius: 10,
                  border: status === "SUCCESS" ? "1px solid #1b7f3a" : "1px solid #b45309",
                  background: status === "SUCCESS" ? "#eaf7ee" : "#fff4e5",
                  color: "#111", // ✅ force readable text
                }}
              >
                <b>Status:</b> {status}
                <div style={{ fontSize: 14, marginTop: 6, opacity: 0.95, color: "#111" }}>
                  Transfer ID: {transferResult.id} • Amount: {transferResult.amount} • CreatedAt:{" "}
                  {transferResult.createdAt}
                </div>
                {status === "FAILED" && (
                  <div style={{ marginTop: 6, color: "#111" }}>
                    Reason: <b>insufficient funds</b>
                  </div>
                )}
              </div>
            )}
          </div>
        </>
      )}

      {error && <p style={{ color: "red" }}>{error}</p>}

      <h3>Customers</h3>
      <pre>{JSON.stringify(customers, null, 2)}</pre>

      <h3>Accounts</h3>
      <pre>{JSON.stringify(accounts, null, 2)}</pre>

      <h3>Transfer Result</h3>
      <pre>{JSON.stringify(transferResult, null, 2)}</pre>
    </div>
  );
}

export default App;