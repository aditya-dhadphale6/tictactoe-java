const API_BASE = "/api";

async function fetchState() {
  const res = await fetch(API_BASE + "/state");
  return res.json();
}

async function postMove(pos) {
  const res = await fetch(API_BASE + "/move", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ pos })
  });
  return res.json();
}

async function postReset() {
  const res = await fetch(API_BASE + "/reset", { method: "POST" });
  return res.json();
}

const boardEl = document.getElementById("board");
const statusEl = document.getElementById("status");
const resetBtn = document.getElementById("resetBtn");

function renderState(state) {
  boardEl.innerHTML = "";
  state.board.forEach((cell, i) => {
    const cellEl = document.createElement("div");
    cellEl.className = "cell";
    if (cell === "X" || cell === "O") {
      cellEl.textContent = cell;
      cellEl.classList.add("taken");
    } else {
      cellEl.textContent = "";
      cellEl.dataset.pos = i + 1;
    }
    cellEl.addEventListener("click", onCellClick);
    boardEl.appendChild(cellEl);
  });
  if (state.winner === null) {
    statusEl.textContent = `${state.turn}'s turn — click an empty cell`;
  } else if (state.winner === "draw") {
    statusEl.textContent = "It's a draw!";
  } else {
    statusEl.textContent = `${state.winner} wins!`;
  }
}

async function refresh() {
  try {
    const state = await fetchState();
    renderState(state);
  } catch (e) {
    statusEl.textContent = "Failed to reach server";
    console.error(e);
  }
}

async function onCellClick(e) {
  const pos = e.currentTarget.dataset.pos;
  if (!pos) return;
  try {
    const res = await postMove(Number(pos));
    if (!res.ok) {
      statusEl.textContent = res.message || "Invalid move";
    } else {
      if (res.state) {
        renderState(res.state);
      } else {
        await refresh();
      }
    }
  } catch (err) {
    statusEl.textContent = "Network error";
    console.error(err);
  }
}

resetBtn.addEventListener("click", async () => {
  await postReset();
  await refresh();
});

// initial load
refresh();
