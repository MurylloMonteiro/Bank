import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

interface Account { username: string; numberAccount: string; pixKey: string; balance: number; negativeBalance: number; }
interface Movement { id: string; type: string; dateTime: string; transationValue: number; pixKeyEnvoy: string; pixKeyReceived: string; numberAccountEnvoy: string; numberAccoutReceived: string; }
type Action = 'deposit' | 'withdraw' | 'pix' | null;

@Component({
  selector: 'app-root', standalone: true, imports: [CommonModule, FormsModule],
  template: `
  @if (!token) {
    <main class="auth-shell">
      <section class="auth-story"><a class="brand brand-light" href="#"><span class="brand-mark">r</span> ratbank</a><div class="story-copy"><span class="eyebrow">SIMPLES COMO DEVERIA SER</span><h1>Seu dinheiro<br>no seu <em>ritmo.</em></h1><p>Uma conta feita para deixar a sua vida financeira mais leve.</p><div class="story-note"><span class="note-icon">✳</span><span>Uma simulação bancária para aprender, testar e crescer.</span></div></div><div class="story-footer">UM PROJETO EDUCACIONAL · 2026</div><div class="story-orb orb-one"></div><div class="story-orb orb-two"></div></section>
      <section class="auth-form-wrap"><div class="auth-form">
        <div class="mobile-brand"><a class="brand" href="#"><span class="brand-mark">r</span> ratbank</a></div>
        @if (!registering) { <span class="eyebrow">BEM-VINDO DE VOLTA</span><h2>Que bom ter você aqui.</h2><p class="muted">Acesse sua conta para continuar.</p> }
        @else { <span class="eyebrow">COMECE POR AQUI</span><h2>Abra sua conta.</h2><p class="muted">É rapidinho — seus dados ficam por aqui.</p> }
        @if (registering) {
          <label>Seu nome<input [(ngModel)]="form.username" placeholder="Como podemos te chamar?"></label>
          <label>E-mail<input [(ngModel)]="form.email" type="email" placeholder="voce@email.com"></label>
          <label>CPF<input [(ngModel)]="form.cpf" placeholder="000.000.000-00"></label>
          <label>Celular <span class="optional">opcional</span><input [(ngModel)]="form.phone_number" placeholder="(00) 00000-0000"></label>
        } @else { <label>E-mail<input [(ngModel)]="form.email" type="email" placeholder="voce@email.com" (keyup.enter)="submitAuth()"></label> }
        <label>Senha<input [(ngModel)]="form.password" type="password" [placeholder]="registering ? 'Mínimo de 8 caracteres' : 'Sua senha'" (keyup.enter)="submitAuth()"></label>
        @if (error) { <div class="alert">{{error}}</div> }
        @if (notice) { <div class="alert success">{{notice}}</div> }
        <button class="button primary full" (click)="submitAuth()" [disabled]="busy">{{busy ? 'Só um instante…' : registering ? 'Criar minha conta' : 'Entrar na minha conta'}} <span>↗</span></button>
        <p class="switch-auth">{{registering ? 'Já tem uma conta?' : 'Ainda não tem conta?'}} <button (click)="toggleAuth()">{{registering ? 'Fazer login' : 'Abra a sua'}}</button></p>
        <p class="disclaimer">Ratbank é uma simulação educacional. Não é uma instituição financeira.</p>
      </div></section>
    </main>
  } @else {
    <div class="app-shell">
      <aside class="sidebar" [class.open]="menuOpen"><a class="brand" href="#"><span class="brand-mark">r</span> ratbank</a>
        <div class="nav-label">SUA CONTA</div><button class="nav-item active"><span>◫</span> Visão geral</button><button class="nav-item" (click)="openAction('pix')"><span>↗</span> Transferir</button><button class="nav-item" (click)="openAction('deposit')"><span>＋</span> Depositar</button><button class="nav-item" (click)="openAction('withdraw')"><span>↙</span> Sacar</button>
        <div class="sidebar-bottom"><div class="help-card"><span class="help-icon">✳</span><b>Aprendendo juntos.</b><p>Este banco é uma simulação para fins educacionais.</p></div><button class="profile" (click)="logout()"><span class="avatar">{{account?.username?.charAt(0) || 'R'}}</span><span class="profile-info"><b>{{account?.username}}</b><small>Sair da conta</small></span><span class="logout-icon">↗</span></button></div>
      </aside>
      <main class="main-area"><header class="topbar"><button class="menu-button" (click)="menuOpen = !menuOpen">☰</button><span class="breadcrumb">Minha conta <span>/</span> Visão geral</span><div class="topbar-right"><span class="status-dot"></span><span>Conta ativa</span><span class="top-avatar">{{account?.username?.charAt(0) || 'R'}}</span></div></header>
        <section class="dashboard"><div class="welcome-row"><div><span class="eyebrow">{{today | date:"EEEE, d 'de' MMMM"}}</span><h1>Olá, {{firstName}} <span class="wave">✳</span></h1><p class="muted">Aqui está um resumo da sua vida financeira.</p></div><button class="button primary" (click)="openAction('pix')"><span>↗</span> Fazer transferência</button></div>
          @if (error) { <div class="alert dashboard-alert">{{error}} <button (click)="error = ''">×</button></div> }
          <div class="overview-grid"><article class="balance-card"><div class="card-top"><span>Saldo disponível</span><button class="eye-button" (click)="hideBalance = !hideBalance">{{hideBalance ? '◉' : '◎'}}</button></div><div class="balance-value">{{hideBalance ? 'R$ ••••••' : ((account?.balance || 0) | currency:'BRL')}}</div><div class="balance-foot"><span class="balance-pill"><i></i> Conta corrente</span><span>Ag. 0001 · Cc. {{account?.numberAccount}}</span></div><div class="balance-shape"></div><div class="balance-shape second"></div></article>
            <article class="info-card"><div class="info-icon pix-icon">⌁</div><div><span class="info-label">Sua chave PIX</span><b class="pix-key">{{account?.pixKey}}</b><button class="copy-link" (click)="copyPix()">{{copied ? 'Copiado!' : 'Copiar chave'}} <span>⧉</span></button></div></article>
            <article class="info-card account-info"><div class="info-icon account-icon">▤</div><div><span class="info-label">Número da conta</span><b class="pix-key">{{account?.numberAccount}}</b><span class="account-sub">Conta pessoal · Ratbank</span></div></article></div>
          <div class="section-heading"><div><h2>Movimentações</h2><p class="muted">Acompanhe as entradas e saídas da sua conta.</p></div><button class="refresh-button" (click)="loadData()" title="Atualizar">↻</button></div>
          <section class="transactions"><div class="table-head"><span>DESCRIÇÃO</span><span>DATA</span><span>VALOR</span></div>
            @for (item of transactions; track item.id) { <div class="transaction-row"><span class="transaction-main"><i class="transaction-icon" [class.incoming]="isIncoming(item)">{{isIncoming(item) ? '↙' : item.type === 'SAQUE' ? '↗' : '↗'}}</i><span><b>{{description(item)}}</b><small>{{movementDetail(item)}}</small></span></span><span class="transaction-date">{{item.dateTime | date:'dd MMM, HH:mm'}}</span><strong class="transaction-amount" [class.incoming-text]="isIncoming(item)">{{isIncoming(item) ? '+' : '−'}} {{item.transationValue | currency:'BRL'}}</strong></div> }
            @if (!transactions.length) { <div class="empty-state"><span>✳</span><b>Tudo começa com um primeiro movimento.</b><p>Faça um depósito ou transfira para uma chave PIX.</p><button class="button secondary" (click)="openAction('deposit')">Fazer primeiro depósito</button></div> }
          </section><footer class="page-footer">Ratbank · Uma experiência de aprendizado financeiro <span>Dados simulados para fins educacionais</span></footer>
        </section>
      </main>
    </div>
    @if (action) { <div class="modal-backdrop" (click)="closeAction()"><section class="modal" (click)="$event.stopPropagation()"><button class="modal-close" (click)="closeAction()">×</button><div class="modal-icon">{{action === 'deposit' ? '＋' : action === 'withdraw' ? '↙' : '↗'}}</div><span class="eyebrow">{{action === 'deposit' ? 'ADICIONE À SUA CONTA' : action === 'withdraw' ? 'RETIRE DA SUA CONTA' : 'PIX · ENVIO IMEDIATO'}}</span><h2>{{action === 'deposit' ? 'Fazer depósito' : action === 'withdraw' ? 'Fazer saque' : 'Nova transferência'}}</h2><p class="muted">{{action === 'pix' ? 'Informe a chave PIX de quem vai receber.' : 'Informe o valor da movimentação.'}}</p>
      @if (action === 'pix') { <label>Chave PIX do destinatário<input [(ngModel)]="pixKey" placeholder="CPF, e-mail ou chave PIX"></label> }
      <label>Valor<input [(ngModel)]="amount" type="number" min="0.01" step="0.01" placeholder="0,00"></label>
      @if (modalError) { <div class="alert">{{modalError}}</div> }
      <button class="button primary full" (click)="submitAction()" [disabled]="busy">{{busy ? 'Processando…' : 'Confirmar ' + (action === 'pix' ? 'transferência' : action === 'deposit' ? 'depósito' : 'saque')}} <span>↗</span></button><p class="modal-note">Apenas uma simulação. Nenhum dinheiro real é movimentado.</p>
    </section></div> }
  }`,
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  private readonly api = 'http://localhost:8080';
  token = localStorage.getItem('ratbank-token') || '';
  account: Account | null = null;
  transactions: Movement[] = [];
  registering = false; busy = false; error = ''; notice = ''; modalError = '';
  action: Action = null; amount: number | null = null; pixKey = '';
  hideBalance = false; copied = false; menuOpen = false; today = new Date();
  form = { username: '', email: '', cpf: '', phone_number: '', password: '' };
  constructor(private http: HttpClient) {}
  ngOnInit(): void { if (this.token) this.loadData(); }
  get firstName(): string { return this.account?.username?.trim().split(' ')[0] || 'de volta'; }
  private headers(): HttpHeaders { return new HttpHeaders({ Authorization: `Bearer ${this.token}` }); }
  toggleAuth(): void { this.registering = !this.registering; this.error = ''; }
  submitAuth(): void {
    this.error = ''; this.notice = ''; this.busy = true;
    if (this.registering) {
      this.http.post(this.api + '/auth/create-account', this.form, { responseType: 'text' }).subscribe({ next: () => {
        this.busy = false; this.registering = false; this.form.password = ''; this.notice = 'Conta criada! Entre com seu e-mail e senha.';
      }, error: e => { this.busy = false; this.error = this.message(e); } });
    } else {
      this.http.post<{token: string}>(this.api + '/auth/login', { email: this.form.email, password: this.form.password }).subscribe({ next: result => {
        this.busy = false; this.token = result.token; localStorage.setItem('ratbank-token', this.token); this.loadData();
      }, error: e => { this.busy = false; this.error = this.message(e); } });
    }
  }
  loadData(): void {
    if (!this.token) return;
    this.http.get<Account>(this.api + '/account', { headers: this.headers() }).subscribe({ next: value => this.account = value, error: e => { if (e.status === 401) this.logout(); else this.error = this.message(e); } });
    this.http.get<Movement[]>(this.api + '/account/transactions', { headers: this.headers() }).subscribe({ next: value => this.transactions = value, error: e => { if (e.status === 401) this.logout(); } });
  }
  openAction(action: Exclude<Action, null>): void { this.action = action; this.amount = null; this.pixKey = ''; this.modalError = ''; this.menuOpen = false; }
  closeAction(): void { if (!this.busy) this.action = null; }
  submitAction(): void {
    if (!this.action || !this.amount || this.amount <= 0) { this.modalError = 'Digite um valor maior que zero.'; return; }
    const current = this.action; this.busy = true; this.modalError = '';
    let request: Observable<string>;
    if (current === 'pix') request = this.http.post(this.api + '/transation', { pixReceived: this.pixKey, value: this.amount }, { headers: this.headers(), responseType: 'text' });
    else request = this.http.put(this.api + '/account/' + current, { value: this.amount }, { headers: this.headers(), responseType: 'text' });
    request.subscribe({ next: () => { this.busy = false; this.action = null; this.loadData(); }, error: e => { this.busy = false; this.modalError = this.message(e); } });
  }
  isIncoming(item: Movement): boolean { return item.type === 'DEPOSITO' || (item.type === 'PIX' && item.numberAccoutReceived === this.account?.numberAccount); }
  description(item: Movement): string { return item.type === 'DEPOSITO' ? 'Depósito em conta' : item.type === 'SAQUE' ? 'Saque em conta' : this.isIncoming(item) ? 'PIX recebido' : 'PIX enviado'; }
  movementDetail(item: Movement): string { return item.type === 'PIX' ? (this.isIncoming(item) ? 'De ' + item.pixKeyEnvoy : 'Para ' + item.pixKeyReceived) : 'Conta corrente'; }
  copyPix(): void { if (this.account?.pixKey) navigator.clipboard?.writeText(this.account.pixKey).then(() => { this.copied = true; setTimeout(() => this.copied = false, 1800); }); }
  logout(): void { localStorage.removeItem('ratbank-token'); this.token = ''; this.account = null; this.transactions = []; this.form.password = ''; }
  private message(error: HttpErrorResponse): string { return typeof error.error === 'string' ? error.error : error.error?.message || 'Não foi possível concluir. Verifique os dados e tente novamente.'; }
}
