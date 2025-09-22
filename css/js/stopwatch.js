// File: js/stopwatch.js
(() => {
  const startBtn = document.getElementById('startBtn');
  const resetBtn = document.getElementById('resetBtn');
  const lapBtn = document.getElementById('lapBtn');
  const display = document.getElementById('display');
  const laps = document.getElementById('laps');
  const copyBtn = document.getElementById('copyBtn');
  const downloadBtn = document.getElementById('downloadBtn');

  let running = false;
  let startTs = 0;
  let elapsedBefore = 0; // ms
  let rafId = null;

  function fmt(ms) {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    const centi = Math.floor((ms % 1000) / 10);
    return `${String(minutes).padStart(2,'0')}:${String(seconds).padStart(2,'0')}.${String(centi).padStart(2,'0')}`;
  }

  function update() {
    const now = performance.now();
    const elapsed = Math.floor((now - startTs) + elapsedBefore);
    display.textContent = fmt(elapsed);
    rafId = requestAnimationFrame(update);
  }

  startBtn.addEventListener('click', () => {
    if (!running) {
      // start
      running = true;
      startTs = performance.now();
      rafId = requestAnimationFrame(update);
      startBtn.textContent = 'Pause';
    } else {
      // pause
      running = false;
      cancelAnimationFrame(rafId);
      elapsedBefore += Math.floor(performance.now() - startTs);
      startBtn.textContent = 'Start';
    }
  });

  resetBtn.addEventListener('click', () => {
    running = false;
    cancelAnimationFrame(rafId);
    elapsedBefore = 0;
    startTs = 0;
    display.textContent = fmt(0);
    startBtn.textContent = 'Start';
    laps.innerHTML = '';
  });

  lapBtn.addEventListener('click', () => {
    const text = display.textContent;
    const li = document.createElement('li');
    li.textContent = `Lap ${laps.children.length + 1} — ${text}`;
    laps.prepend(li);
  });

  copyBtn.addEventListener('click', async () => {
    try {
      await navigator.clipboard.writeText(display.textContent);
      copyBtn.textContent = 'Copied!';
      setTimeout(()=> copyBtn.textContent = 'Copy time', 1200);
    } catch (e) {
      copyBtn.textContent = 'Failed';
      setTimeout(()=> copyBtn.textContent = 'Copy time', 1200);
    }
  });

  downloadBtn.addEventListener('click', () => {
    // use html2canvas included via CDN to snapshot the .glass-card
    const card = document.querySelector('.glass-card');
    html2canvas(card, { scale: 2 }).then(canvas => {
      const a = document.createElement('a');
      a.href = canvas.toDataURL('image/png');
      a.download = `stopwatch-${Date.now()}.png`;
      a.click();
    });
  });

})();
