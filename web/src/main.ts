import './style.css';

const root = document.querySelector<HTMLDivElement>('#app');
if (!root) throw new Error('App root missing');

const params = new URLSearchParams(location.search);
const invitation = params.get('invite');
const backend = (import.meta as ImportMeta & { env: { VITE_API_BASE?: string } }).env.VITE_API_BASE?.replace(/\/$/, '');

root.innerHTML = `
  <main class="shell">
    <div class="mark">⌖</div>
    <p class="eyebrow">PHONE LOCATION · 2GIS</p>
    <h1>Мубодилаи макон бо розигии шумо</h1>
    <p class="lead">Шахсе шуморо барои мубодилаи ихтиёрии макон даъват кардааст.</p>
    <section class="card">
      <h2>Пеш аз идома</h2>
      <ul>
        <li>Ҷойгиршавии шумо танҳо баъди пахш кардани тугма ва иҷозати браузер гирифта мешавад.</li>
        <li>Координатаҳо танҳо ба сервери танзимшуда фиристода мешаванд.</li>
        <li>Шумо метавонед рад кунед ё мубодиларо қатъ намоед.</li>
        <li>Рақами телефон худ аз худ дастрасӣ ба макон намедиҳад.</li>
      </ul>
      <p id="notice" class="notice"></p>
      <button id="accept" class="primary">Ман розӣ ҳастам — иҷозат пурс</button>
      <button id="decline" class="secondary">Рад кардан</button>
      <button id="stop" class="secondary" hidden>Қатъ кардани мубодила</button>
    </section>
    <p class="foot">Агар ба даъват бовар надоред, онро рад кунед. Ин саҳифа ҳеҷ гоҳ ҷойгиршавиро дар пасзамина намегирад.</p>
  </main>`;

const notice = document.querySelector<HTMLParagraphElement>('#notice')!;
const accept = document.querySelector<HTMLButtonElement>('#accept')!;
const decline = document.querySelector<HTMLButtonElement>('#decline')!;
const stop = document.querySelector<HTMLButtonElement>('#stop')!;

function setNotice(message: string) { notice.textContent = message; }

if (!invitation) {
  accept.disabled = true;
  setNotice('Пайванди даъват нодуруст аст ё токен надорад.');
} else if (!backend) {
  accept.disabled = true;
  setNotice('Хизматрасонии сервер ҳанӯз танзим нашудааст. Макон гирифта ё фиристода намешавад.');
}

decline.addEventListener('click', () => {
  accept.disabled = true;
  decline.disabled = true;
  setNotice('Шумо даъватро рад кардед. Макони шумо гирифта нашуд.');
});

accept.addEventListener('click', async () => {
  if (!invitation || !backend) return;
  if (!window.isSecureContext) {
    setNotice('Барои бехатарӣ ин саҳифа бояд тавассути HTTPS кушода шавад.');
    return;
  }
  if (!navigator.geolocation) {
    setNotice('Ин браузер геолокатсияро дастгирӣ намекунад.');
    return;
  }
  accept.disabled = true;
  setNotice('Дархости иҷозати ҷойгиршавӣ…');
  navigator.geolocation.getCurrentPosition(async (position) => {
    try {
      const response = await fetch(`${backend}/api/invitations/${encodeURIComponent(invitation)}/accept`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          accuracy: position.coords.accuracy
        })
      });
      if (!response.ok) throw new Error('server');
      const data = await response.json() as { sessionId: string };
      sessionStorage.setItem('locationSessionId', data.sessionId);
      setNotice('Розигӣ сабт шуд. Мубодила фаъол аст танҳо тибқи сиёсати сервер.');
      stop.hidden = false;
    } catch {
      setNotice('Сервер дастраc нест ё даъват эътибор надорад. Макон нигоҳ дошта нашуд.');
      accept.disabled = false;
    }
  }, (error) => {
    const message = error.code === error.PERMISSION_DENIED ? 'Шумо иҷозати ҷойгиршавиро рад кардед.' :
      'Ҷойгиршавӣ дастрас нест. GPS ва танзимоти браузерро санҷед.';
    setNotice(message);
    accept.disabled = false;
  }, { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 });
});

stop.addEventListener('click', async () => {
  const sessionId = sessionStorage.getItem('locationSessionId');
  if (!sessionId || !backend) {
    setNotice('Сессияи фаъол ё сервер ёфт нашуд.');
    return;
  }
  try {
    const response = await fetch(`${backend}/api/sessions/${encodeURIComponent(sessionId)}/revoke`, { method: 'POST' });
    if (!response.ok) throw new Error('server');
    sessionStorage.removeItem('locationSessionId');
    setNotice('Мубодила қатъ карда шуд.');
    stop.hidden = true;
  } catch {
    setNotice('Қатъкунӣ аз сервер тасдиқ нашуд. Пайвастро санҷед.');
  }
});
