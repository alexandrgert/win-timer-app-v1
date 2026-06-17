const { useState, useEffect, useRef } = React;

/* ── Utils ─────────────────────────────────────── */
const pad  = n => String(n).padStart(2, '0');
const fmt  = s => `${pad(Math.floor(s/3600))}:${pad(Math.floor((s%3600)/60))}:${pad(s%60)}`;
const fmtS = s => `${pad(Math.floor(s/3600))}:${pad(Math.floor((s%3600)/60))}`;

function getWeek() {
  const today = new Date();
  const dow = today.getDay();
  const mon = new Date(today);
  mon.setDate(today.getDate() - ((dow + 6) % 7));
  const RU = ['Пн','Вт','Ср','Чт','Пт','Сб','Вс'];
  return Array.from({length:7},(_,i)=>{
    const d = new Date(mon); d.setDate(mon.getDate()+i);
    return { l: RU[i], n: d.getDate(), today: d.toDateString()===today.toDateString() };
  });
}
const WEEK = getWeek();

/* ── Seed ──────────────────────────────────────── */
const SEED = [
  { id:'1', title:'Автоматизация процесса увольнения | ООО Торик',
    status:'running', todaySeconds:0, totalSeconds:3631, sessions:[
      {id:'s1',start:'09:00',end:'10:01',date:'15.06.2026',duration:3660},
      {id:'s2',start:'14:00',end:'14:30',date:'14.06.2026',duration:1800},
    ]},
  { id:'2', title:'ТЗ по модулю отчётности | ООО Прогресс',
    status:'paused', todaySeconds:5400, totalSeconds:12600, sessions:[
      {id:'s3',start:'08:30',end:'10:00',date:'15.06.2026',duration:5400},
    ]},
  { id:'3', title:'Код-ревью pull-request #143: модуль авторизации',
    status:'todo', todaySeconds:0, totalSeconds:2700, sessions:[]},
  { id:'4', title:'Встреча с клиентом: согласование дизайн-макетов',
    status:'done', todaySeconds:3600, totalSeconds:7200, sessions:[
      {id:'s4',start:'11:00',end:'12:00',date:'15.06.2026',duration:3600},
    ]},
];

const ld  = (k,fb)=>{ try{return JSON.parse(localStorage.getItem(k)||'null')||fb;}catch{return fb;} };
const lds = (k,fb)=>{ try{return localStorage.getItem(k)||fb;}catch{return fb;} };
const ldSec = ()=>{ try{ const s=parseInt(localStorage.getItem('tt5-start')||'0'); return s?Math.max(0,Math.floor((Date.now()-s)/1000)):31; }catch{return 31;} };

/* ── Icons ─────────────────────────────────────── */
const IcTimer    = () => <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"><circle cx="8" cy="9" r="6"/><path d="M8 6v3l2 1.5M8 1v2M5.5 1h5"/></svg>;
const IcFocus    = () => <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"><circle cx="8" cy="8" r="6"/><circle cx="8" cy="8" r="2"/></svg>;
const IcSettings = () => <svg width="15" height="15" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"><circle cx="8" cy="8" r="2.5"/><path d="M8 1.5v1.5M8 13v1.5M1.5 8H3M13 8h1.5M3.4 3.4l1.1 1.1M11.5 11.5l1.1 1.1M12.6 3.4l-1.1 1.1M4.5 11.5l-1.1 1.1"/></svg>;
const IcPlus     = () => <svg width="11" height="11" viewBox="0 0 11 11" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round"><path d="M5.5 2v7M2 5.5h7"/></svg>;
const IcTrash    = () => <svg width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round"><path d="M1.5 3.5h11M5 3.5V2.5a.5.5 0 0 1 .5-.5h3a.5.5 0 0 1 .5.5v1M3.5 3.5l.8 9h5.4l.8-9"/></svg>;
const IcHistory  = () => <svg width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round"><circle cx="7" cy="8" r="5"/><path d="M7 5.5V8l1.5 1.5M5 1.5L7 3.5l2-2"/></svg>;
const IcPlay     = () => <svg width="7" height="9" viewBox="0 0 7 9" fill="currentColor"><path d="M0 0l7 4.5L0 9V0z"/></svg>;
const IcPause    = () => <svg width="8" height="10" viewBox="0 0 8 10" fill="currentColor"><rect x="0" y="0" width="3" height="10" rx="1"/><rect x="5" y="0" width="3" height="10" rx="1"/></svg>;
const IcClock    = () => <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round"><circle cx="12" cy="13" r="8"/><path d="M12 9v4l3 3M10 2h4M12 2v2.5"/></svg>;
const IcCal      = () => <svg width="13" height="13" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round"><rect x="1" y="2.5" width="12" height="10" rx="2"/><path d="M1 6h12M4.5 1v2.5M9.5 1v2.5"/></svg>;
const IcChevL    = () => <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"><path d="M7.5 3L4.5 6l3 3"/></svg>;
const IcChevR    = () => <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"><path d="M4.5 3L7.5 6l-3 3"/></svg>;

const BADGE_LBL = { running:'В работе', paused:'Пауза', todo:'Запланировано', done:'Готово' };

const RU_MONTHS = ['Январь','Февраль','Март','Апрель','Май','Июнь',
  'Июль','Август','Сентябрь','Октябрь','Ноябрь','Декабрь'];
const RU_DOW_S  = ['Пн','Вт','Ср','Чт','Пт','Сб','Вс'];

function MiniCalendar({ selected, onChange }) {
  const [open, setOpen] = useState(false);
  const [view, setView] = useState(() => { const d=new Date(selected); d.setDate(1); return d; });
  const ref = useRef(null);

  useEffect(() => {
    const h = e => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); };
    if (open) document.addEventListener('mousedown', h);
    return () => document.removeEventListener('mousedown', h);
  }, [open]);

  const cells = () => {
    const year = view.getFullYear(), month = view.getMonth();
    const first = new Date(year, month, 1);
    const startDow = (first.getDay() + 6) % 7; // Mon=0
    const days = [];
    for (let i = 0; i < startDow; i++) {
      const d = new Date(year, month, 1 - startDow + i);
      days.push({ d, other: true });
    }
    const last = new Date(year, month + 1, 0).getDate();
    for (let i = 1; i <= last; i++) days.push({ d: new Date(year, month, i), other: false });
    while (days.length % 7 !== 0) {
      const d = new Date(year, month + 1, days.length - last - startDow + 1);
      days.push({ d, other: true });
    }
    return days;
  };

  const todayStr = new Date().toDateString();
  const selStr   = selected.toDateString();
  const selFmt   = selected.toLocaleDateString('ru-RU', { day: 'numeric', month: 'long' });

  const prevMonth = () => setView(v => new Date(v.getFullYear(), v.getMonth()-1, 1));
  const nextMonth = () => setView(v => new Date(v.getFullYear(), v.getMonth()+1, 1));

  return (
    <div className="cal-wrap" ref={ref}>
      <button className={`cal-btn${open?' open':''}`} onClick={()=>setOpen(o=>!o)}>
        <IcCal /> {selFmt}
      </button>
      {open && (
        <div className="cal-popup">
          <div className="cal-head">
            <button className="cal-nav" onClick={prevMonth}><IcChevL /></button>
            <span className="cal-head-lbl">{RU_MONTHS[view.getMonth()]} {view.getFullYear()}</span>
            <button className="cal-nav" onClick={nextMonth}><IcChevR /></button>
          </div>
          <div className="cal-grid">
            {RU_DOW_S.map(d=><div key={d} className="cal-dow">{d}</div>)}
            {cells().map(({d,other},i)=>(
              <button key={i}
                className={[
                  'cal-cell',
                  other?'other':'',
                  d.toDateString()===todayStr?'today':'',
                  d.toDateString()===selStr?'selected':''
                ].join(' ').trim()}
                onClick={()=>{ onChange(d); setOpen(false); }}>
                {d.getDate()}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

/* ── Task row ───────────────────────────────────── */
function TaskRow({ task, isActive, onStart, onStop, onHistory, onRemove, onComplete }) {
  const isRun  = task.status === 'running';
  const isDone = task.status === 'done';
  const canPlay = task.status === 'todo' || task.status === 'paused';

  return (
    <div className={`trow ${task.status}`}>
      <span className={`tdot ${task.status}`} />
      <span className={`tbadge ${task.status}`}>{BADGE_LBL[task.status]}</span>
      <span className="tname" title={task.title}>{task.title}</span>

      <div className="ttimes">
        <span className="tti-lbl">сег.</span>
        <span className={`tti-val${isActive?' live':''}`}>{fmtS(task.todaySeconds)}</span>
        <span className="tti-sep">·</span>
        <span className="tti-lbl">всего</span>
        <span className="tti-val">{fmtS(task.totalSeconds)}</span>
      </div>

      <div className="trow-actions">
        {!isDone && (
          <button className="ia" title="История"
            onClick={e=>{e.stopPropagation();onHistory();}}>
            <IcHistory />
          </button>
        )}
        {!isDone && (
          <button className="ia-lnk" title="Открыть в Битрикс24"
            onClick={e=>{e.stopPropagation();}}>
            Открыть в Б24
          </button>
        )}
        {!isDone && (
          <button className="ia-lnk" title="Завершить"
            onClick={e=>{e.stopPropagation();onComplete();}}>
            Завершить
          </button>
        )}
        <button className="ia danger" title="Удалить"
          onClick={e=>{e.stopPropagation();onRemove();}}>
          <IcTrash />
        </button>
        {isRun && (
          <button className="tplay stop" onClick={e=>{e.stopPropagation();onStop();}}>
            <IcPause /> Стоп
          </button>
        )}
        {canPlay && (
          <button className="tplay go" onClick={e=>{e.stopPropagation();onStart();}}>
            <IcPlay /> Старт
          </button>
        )}
      </div>
    </div>
  );
}

/* ── Tasks pane ─────────────────────────────────── */
function TasksPane({ tasks, activeId, onStart, onStop, onComplete, onRemove, onHistory, onNew, onPortal }) {
  const [filter, setFilter] = useState('today');
  const [selDate, setSelDate] = useState(new Date());
  const visible = tasks.filter(t=>{
    if(filter==='today')   return t.status!=='done'||t.todaySeconds>0;
    if(filter==='running') return t.status==='running'||t.status==='paused';
    return true;
  });
  return (
    <div className="task-pane">
      <div className="subbar">
        <div className="seg">
          {[['today','Сегодня'],['running','В работе'],['all','Все']].map(([k,l])=>(
            <button key={k} className={`seg-btn${filter===k?' on':''}`}
              onClick={()=>setFilter(k)}>{l}</button>
          ))}
        </div>
        <MiniCalendar selected={selDate} onChange={setSelDate} />
        <div className="sub-sp" />
        <button className="btn-ghost" onClick={onPortal}>С портала</button>
        <button className="btn-accent" onClick={onNew}><IcPlus /> Новая задача</button>
      </div>
      <div className="task-scroll">
        {visible.length===0
          ? <div className="tempty">Задачи не найдены</div>
          : visible.map(t=>(
            <TaskRow key={t.id} task={t} isActive={t.id===activeId}
              onStart={()=>onStart(t.id)} onStop={onStop}
              onHistory={()=>onHistory(t.id)} onRemove={()=>onRemove(t.id)}
              onComplete={()=>onComplete(t.id)} />
          ))
        }
      </div>
    </div>
  );
}

/* ── Timer panel ────────────────────────────────── */
function TimerPanel({ task, sessionSec, isRun, onStop, onStart, onComplete }) {
  if (!task) return (
    <div className="timer-panel">
      <div className="timer-empty">
        <div className="timer-empty-ico"><IcClock /></div>
        <p className="timer-empty-txt">Выберите задачу<br/>и нажмите <b style={{fontWeight:500}}>Старт</b></p>
      </div>
    </div>
  );
  const pct = Math.min((sessionSec/2400)*100, 100);
  return (
    <div className={`timer-panel${isRun?' running':''}`}>
      <div className="timer-lbl">Таймер</div>
      <div className="timer-card">
        <div className="tcard-name">{task.title}</div>
        <div className="tcard-time">{fmt(sessionSec)}</div>
        <div className="tcard-sub">
          <div className="tcs-item">
            <span className="tcs-lbl">Сегодня</span>
            <span className="tcs-val">{fmtS(task.todaySeconds)}</span>
          </div>
          <div className="tcs-item">
            <span className="tcs-lbl">Всего</span>
            <span className="tcs-val">{fmtS(task.totalSeconds)}</span>
          </div>
        </div>
      </div>
      <div className="timer-prog">
        <div className="timer-prog-bar" style={{width:`${pct}%`}} />
      </div>
      <div className="timer-sp" />
      <div className="timer-btns">
        {isRun
          ? <button className="tbig tstop"  onClick={onStop}>Стоп</button>
          : <button className="tbig tstart" onClick={onStart}>Старт</button>
        }
        <button className="tbig tcomp" onClick={onComplete}>Завершить задачу</button>
      </div>
    </div>
  );
}

/* ── Focus ──────────────────────────────────────── */
function FocusView() {
  const DURS=[5,10,15,25,40];
  const [mins,setMins]=useState(25);
  const [running,setRunning]=useState(false);
  const [secs,setSecs]=useState(25*60);
  const ref=useRef(null);
  useEffect(()=>{
    clearInterval(ref.current);
    if(running){ref.current=setInterval(()=>setSecs(s=>{if(s<=1){setRunning(false);return 0;}return s-1;}),1000);}
    return()=>clearInterval(ref.current);
  },[running]);
  const setDur=m=>{if(!running){setMins(m);setSecs(m*60);}};
  const toggle=()=>{if(secs===0)setSecs(mins*60);setRunning(r=>!r);};
  return (
    <div className="focus-view">
      <div className="focus-card">
        <div className="focus-lbl">Режим концентрации</div>
        <div className={`focus-time${secs===0?' done':''}`}>{`${pad(Math.floor(secs/60))}:${pad(secs%60)}`}</div>
        <div className="focus-durs">
          {DURS.map(d=><button key={d} className={`fdur${mins===d&&!running?' on':''}`} onClick={()=>setDur(d)}>{d} мин</button>)}
        </div>
        <button className={`focus-go${running?' run':''}`} onClick={toggle}>
          {running?'Пауза':secs===0?'Заново':'Старт'}
        </button>
        {running&&<p className="focus-note">Не переключайтесь — вы в фокусе</p>}
      </div>
    </div>
  );
}

/* ── Modals ─────────────────────────────────────── */
function HistoryModal({ task, onClose }) {
  if (!task) return null;
  const total = task.sessions.reduce((s,x)=>s+x.duration,0);
  return (
    <div className="overlay" onClick={e=>e.target===e.currentTarget&&onClose()}>
      <div className="modal">
        <div className="mhdr">
          <div className="mtitle">История сессий</div>
          <button className="mclose" onClick={onClose}>×</button>
        </div>
        <div className="mbody">
          <p style={{fontSize:12,color:'var(--t2)',marginBottom:14,lineHeight:1.5,fontWeight:300}}>{task.title}</p>
          {task.sessions.length===0
            ? <p style={{color:'var(--t3)',fontSize:12}}>Записей нет</p>
            : task.sessions.map(s=>(
              <div key={s.id} className="srow">
                <span className="srow-date">{s.date}</span>
                <span className="srow-range">{s.start} — {s.end}</span>
                <span className="srow-dur">{fmtS(s.duration)}</span>
              </div>
            ))
          }
          {task.sessions.length>0&&<div className="stotal">Итого:&nbsp;<b>{fmtS(total)}</b></div>}
        </div>
        <div className="mfoot"><button className="btn-ms" onClick={onClose}>Закрыть</button></div>
      </div>
    </div>
  );
}

function NewTaskModal({ onAdd, onClose }) {
  const [val,setVal]=useState('');
  const add=()=>val.trim()&&onAdd(val.trim());
  return (
    <div className="overlay" onClick={e=>e.target===e.currentTarget&&onClose()}>
      <div className="modal">
        <div className="mhdr">
          <div className="mtitle">Новая задача</div>
          <button className="mclose" onClick={onClose}>×</button>
        </div>
        <div className="mbody">
          <textarea className="ntarea" placeholder="Название задачи…" value={val} autoFocus
            onChange={e=>setVal(e.target.value)}
            onKeyDown={e=>{if(e.key==='Enter'&&(e.ctrlKey||e.metaKey))add();if(e.key==='Escape')onClose();}}/>
          <p className="nhint">Ctrl + Enter — добавить · Esc — отмена</p>
        </div>
        <div className="mfoot">
          <button className="btn-ms" onClick={onClose}>Отмена</button>
          <button className="btn-mp" onClick={add} disabled={!val.trim()}>Добавить</button>
        </div>
      </div>
    </div>
  );
}

function SettingsModal({ onClose }) {
  const [val,setVal]=useState(40);
  return (
    <div className="overlay" onClick={e=>e.target===e.currentTarget&&onClose()}>
      <div className="modal" style={{minWidth:360,maxWidth:400}}>
        <div className="mhdr">
          <div className="mtitle">Настройки</div>
          <button className="mclose" onClick={onClose}>×</button>
        </div>
        <div className="mbody">
          <div className="sfield">
            <div className="slbl">Интервал напоминания</div>
            <div className="srange-row">
              <input type="range" min="5" max="60" step="5" value={val}
                onChange={e=>setVal(Number(e.target.value))}/>
              <span className="srange-val">{val} мин</span>
            </div>
            <p className="shint">Через {val} мин. работы появится вопрос «Продолжаете?». Без ответа — таймер остановится.</p>
          </div>
        </div>
        <div className="mfoot">
          <button className="btn-ms" onClick={onClose}>Отмена</button>
          <button className="btn-mp" onClick={onClose}>Сохранить</button>
        </div>
      </div>
    </div>
  );
}

/* ── App ────────────────────────────────────────── */
function App() {
  const [tasks,      setTasks]      = useState(()=>ld('tt5-tasks',SEED));
  const [activeId,   setActiveId]   = useState(()=>lds('tt5-active','1'));
  const [sessionSec, setSessionSec] = useState(ldSec);
  const [tab,        setTab]        = useState('tasks');
  const [historyId,  setHistoryId]  = useState(null);
  const [showNew,    setShowNew]    = useState(false);
  const [showSett,   setShowSett]   = useState(false);
  const [toast,      setToast]      = useState('');
  const [toastKey,   setToastKey]   = useState(0);

  const timerRef=useRef(null), toastRef=useRef(null), aidRef=useRef(activeId);
  aidRef.current=activeId;

  const activeTask = tasks.find(t=>t.id===activeId)||null;
  const isRun      = activeTask?.status==='running';
  const todayTotal = tasks.reduce((s,t)=>s+t.todaySeconds,0);

  useEffect(()=>{
    clearInterval(timerRef.current);
    if(isRun){
      timerRef.current=setInterval(()=>{
        setSessionSec(s=>s+1);
        setTasks(prev=>prev.map(t=>t.id===aidRef.current
          ?{...t,todaySeconds:t.todaySeconds+1,totalSeconds:t.totalSeconds+1}:t));
      },1000);
    }
    return()=>clearInterval(timerRef.current);
  },[isRun]);

  useEffect(()=>{localStorage.setItem('tt5-tasks',JSON.stringify(tasks));},[tasks]);
  useEffect(()=>{localStorage.setItem('tt5-active',activeId||'');},[activeId]);

  useEffect(()=>{
    const h=e=>{
      if(e.code!=='Space')return;
      if(['INPUT','TEXTAREA'].includes(document.activeElement.tagName))return;
      e.preventDefault();
      if(!activeTask||activeTask.status==='done')return;
      isRun?handleStop():handleStart(activeId);
    };
    window.addEventListener('keydown',h);
    return()=>window.removeEventListener('keydown',h);
  },[isRun,activeId,activeTask]);

  const showToast=msg=>{
    setToast(msg);setToastKey(k=>k+1);
    clearTimeout(toastRef.current);
    toastRef.current=setTimeout(()=>setToast(''),2400);
  };

  const handleStart=id=>{
    const prev=tasks.find(t=>t.status==='running');
    localStorage.setItem('tt5-start',String(Date.now()));
    setTasks(p=>p.map(t=>({...t,status:t.status==='running'?'paused':t.id===id?'running':t.status})));
    setActiveId(id);setSessionSec(0);
    if(prev&&prev.id!==id)showToast(`На паузе: ${prev.title.slice(0,36)}…`);
  };
  const handleStop=()=>{
    localStorage.removeItem('tt5-start');
    setTasks(p=>p.map(t=>t.id===activeId?{...t,status:'paused'}:t));
  };
  const handleComplete=id=>{
    localStorage.removeItem('tt5-start');
    setTasks(p=>p.map(t=>t.id===id?{...t,status:'done'}:t));
    if(id===activeId){setActiveId(null);setSessionSec(0);}
    showToast('Задача завершена');
  };
  const handleRemove=id=>{
    setTasks(p=>p.filter(t=>t.id!==id));
    if(id===activeId){setActiveId(null);setSessionSec(0);}
  };
  const handleAdd=title=>{
    setTasks(p=>[...p,{id:String(Date.now()),title,status:'todo',todaySeconds:0,totalSeconds:0,sessions:[]}]);
    setShowNew(false);showToast('Задача добавлена');
  };

  return (
    <div className="app">
      <nav className="sidebar">
        <div className="sb-logo"><IcTimer /></div>
        <button className={`sb-item${tab==='tasks'?' on':''}`} onClick={()=>setTab('tasks')} title="Задачи"><IcTimer /></button>
        <button className={`sb-item${tab==='focus'?' on':''}`} onClick={()=>setTab('focus')} title="Фокус"><IcFocus /></button>
        <div className="sb-sp" />
        <button className="sb-item" onClick={()=>setShowSett(true)} title="Настройки"><IcSettings /></button>
      </nav>

      <div className="main">
        <div className="topbar">
          <span className="tb-title">Задачи на сегодня</span>
          <div className="tb-sp" />

          <div className="tb-total">
            <span className="tb-total-lbl">Итого сегодня</span>
            <span className="tb-total-val">{fmtS(todayTotal)}</span>
          </div>
        </div>

        <div className="content-row">
          {tab==='tasks' ? (
            <>
              <TasksPane
                tasks={tasks} activeId={activeId}
                onStart={handleStart} onStop={handleStop}
                onComplete={handleComplete} onRemove={handleRemove}
                onHistory={id=>setHistoryId(id)}
                onNew={()=>setShowNew(true)}
                onPortal={()=>showToast('Подключение к порталу…')}
              />
              <TimerPanel
                task={activeTask} sessionSec={sessionSec} isRun={isRun}
                onStop={handleStop}
                onStart={()=>handleStart(activeId)}
                onComplete={()=>handleComplete(activeId)}
              />
            </>
          ) : <FocusView /> }
        </div>
      </div>

      {historyId && <HistoryModal task={tasks.find(t=>t.id===historyId)} onClose={()=>setHistoryId(null)} />}
      {showNew   && <NewTaskModal  onAdd={handleAdd} onClose={()=>setShowNew(false)} />}
      {showSett  && <SettingsModal onClose={()=>setShowSett(false)} />}
      {toast     && <div key={toastKey} className="toast">{toast}</div>}
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
