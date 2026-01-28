const BASE_URL = "http://127.0.0.1:8080";

async function readError(res) {
  const text = await res.text().catch(() => "");
  try {
    return text ? JSON.parse(text) : null;
  } catch {
    return text || null;
  }
}

export async function login(username, password) {
  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!res.ok) {
    const err = await readError(res);
    throw new Error(`Login failed: ${res.status} ${typeof err === "string" ? err : JSON.stringify(err)}`);
  }

  return res.json(); // { token }
}

export async function getCustomers(token) {
  const res = await fetch(`${BASE_URL}/customers`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!res.ok) {
    const err = await readError(res);
    throw new Error(`Customers failed: ${res.status} ${typeof err === "string" ? err : JSON.stringify(err)}`);
  }

  return res.json();
}

export async function getAccounts(token, customerId) {
  const res = await fetch(`${BASE_URL}/accounts?customerId=${encodeURIComponent(customerId)}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!res.ok) {
    const err = await readError(res);
    throw new Error(`Accounts failed: ${res.status} ${typeof err === "string" ? err : JSON.stringify(err)}`);
  }

  return res.json();
}

export async function createTransfer(token, fromAccountId, toAccountId, amount) {
  const res = await fetch(`${BASE_URL}/transfers`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      fromAccountId: Number(fromAccountId),
      toAccountId: Number(toAccountId),
      amount: Number(amount),
    }),
  });

  if (!res.ok) {
    const err = await readError(res);
    throw new Error(`Transfer failed: ${res.status} ${typeof err === "string" ? err : JSON.stringify(err)}`);
  }

  return res.json(); // { status, ... }
}