// cSpell:ignore dashboard paiements etudiant formateur brouillon supprime

import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminDashboardOverview, AdminDashboardPayment, AdminDashboardResponse, AdminService } from '../../services/admin';
import { Cours, EtatCours } from '../../services/cours';
import { FormateurResponse } from '../../services/formateur';

interface ActiviteItem {
  type: 'cours' | 'formateur' | 'paiement' | 'certificat';
  message: string;
  temps: string;
  icon: string;
  color: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  templateUrl: './admin-dashboard.html',
  styleUrls: ['./admin-dashboard.scss'],
  imports: [CommonModule],
})
export class AdminDashboard implements OnInit, AfterViewInit {
  @ViewChild('chartCoursRef') chartCoursRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('chartPipelineRef') chartPipelineRef!: ElementRef<HTMLCanvasElement>;

  loading = true;
  errorMessage = '';

  stats: AdminDashboardOverview = this.getEmptyOverview();
  derniersCours: Cours[] = [];
  topCourses: Cours[] = [];
  pendingPayments: AdminDashboardPayment[] = [];
  dernieresCandidat: FormateurResponse[] = [];
  activites: ActiviteItem[] = [];

  today = new Date().toLocaleDateString('fr-FR', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  constructor(
    private readonly adminService: AdminService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.chargerDonnees();
  }

  ngAfterViewInit(): void {
    // Les graphiques sont dessines une fois les donnees chargees.
  }

  chargerDonnees(): void {
    this.loading = true;
    this.errorMessage = '';

    this.adminService.getDashboard().subscribe({
      next: (data: AdminDashboardResponse) => {
        this.stats = data.overview ?? this.getEmptyOverview();
        this.derniersCours = data.recentCourses ?? [];
        this.topCourses = data.topCourses ?? [];
        this.pendingPayments = data.pendingPayments ?? [];
        this.dernieresCandidat = data.pendingFormateurs ?? [];
        this.construireActivites();
        this.loading = false;
        this.cdr.detectChanges();
        setTimeout(() => this.dessinerCharts(), 80);
      },
      error: (err: unknown) => {
        console.error(err);
        this.errorMessage = 'Impossible de charger les indicateurs admin pour le moment.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  retry(): void {
    this.chargerDonnees();
  }

  get pendingWorkCount(): number {
    return this.stats.coursEnAttenteValidation + this.stats.formateursEnAttente + this.stats.paiementsEnAttente;
  }

  get totalActiveUsers(): number {
    return this.stats.etudiantsActifs + this.stats.formateursActifs;
  }

  get publishedRate(): number {
    return this.stats.totalCours ? Math.round((this.stats.coursPublies / this.stats.totalCours) * 100) : 0;
  }

  formatMoney(value: number | null | undefined): string {
    return new Intl.NumberFormat('fr-TN', {
      style: 'currency',
      currency: 'TND',
      minimumFractionDigits: 2,
    }).format(Number(value ?? 0));
  }

  getStatutLabel(statut: EtatCours): string {
    const labels: Record<EtatCours, string> = {
      BROUILLON: 'Brouillon',
      EN_ATTENTE_VALIDATION: 'En attente',
      PUBLIE: 'Publie',
      SUPPRIME: 'Supprime',
    };
    return labels[statut] ?? statut;
  }

  getStatutClass(statut: EtatCours): string {
    const classes: Record<EtatCours, string> = {
      BROUILLON: 'brouillon',
      EN_ATTENTE_VALIDATION: 'attente',
      PUBLIE: 'publie',
      SUPPRIME: 'supprime',
    };
    return classes[statut] ?? '';
  }

  getPaymentStatusLabel(statut: AdminDashboardPayment['statut']): string {
    const labels: Record<AdminDashboardPayment['statut'], string> = {
      EN_ATTENTE: 'En attente',
      APPROUVE: 'Approuve',
      REFUSE: 'Refuse',
    };
    return labels[statut] ?? statut;
  }

  getPaymentStatusClass(statut: AdminDashboardPayment['statut']): string {
    const classes: Record<AdminDashboardPayment['statut'], string> = {
      EN_ATTENTE: 'attente',
      APPROUVE: 'publie',
      REFUSE: 'supprime',
    };
    return classes[statut] ?? '';
  }

  getInitiales(nom: string): string {
    if (!nom?.trim()) {
      return '?';
    }

    return nom
      .trim()
      .split(' ')
      .map((part) => part[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  getCourseMeta(course: Cours): string {
    const inscriptions = course.nombreInscrits ?? 0;
    const note = Number(course.noteMoyenne ?? 0).toFixed(1);
    return `${inscriptions} inscrits - note ${note}/5`;
  }

  private construireActivites(): void {
    const items: ActiviteItem[] = [];

    this.pendingPayments.slice(0, 2).forEach((payment) => {
      items.push({
        type: 'paiement',
        message: `Paiement en attente pour ${payment.coursTitre} par ${payment.etudiantNom}.`,
        temps: this.getRelativeDateLabel(payment.dateCreation),
        icon: 'PAY',
        color: '#f59e0b',
      });
    });

    this.dernieresCandidat.slice(0, 2).forEach((formateur) => {
      items.push({
        type: 'formateur',
        message: `Candidature formateur de ${formateur.nom} a revoir.`,
        temps: 'A traiter',
        icon: 'NEW',
        color: '#8b5cf6',
      });
    });

    this.derniersCours
      .filter((course) => course.etatPublication === 'EN_ATTENTE_VALIDATION')
      .slice(0, 2)
      .forEach((course) => {
        items.push({
          type: 'cours',
          message: `Validation requise pour la formation "${course.titre}".`,
          temps: this.getRelativeDateLabel(course.dateCreation),
          icon: 'CRS',
          color: '#6366f1',
        });
      });

    if (this.stats.certificatsGeneres > 0) {
      items.push({
        type: 'certificat',
        message: `${this.stats.certificatsGeneres} certificats sont deja disponibles sur la plateforme.`,
        temps: 'Suivi global',
        icon: 'CRT',
        color: '#10b981',
      });
    }

    this.activites = items.slice(0, 6);
  }

  private getRelativeDateLabel(rawDate?: string): string {
    if (!rawDate) {
      return 'Recemment';
    }

    const timestamp = new Date(rawDate).getTime();
    if (Number.isNaN(timestamp)) {
      return 'Recemment';
    }

    const diffMs = Date.now() - timestamp;
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffHours / 24);

    if (diffHours < 1) {
      return 'Il y a quelques minutes';
    }
    if (diffHours < 24) {
      return `Il y a ${diffHours}h`;
    }
    if (diffDays < 7) {
      return `Il y a ${diffDays} jour${diffDays > 1 ? 's' : ''}`;
    }
    return new Date(rawDate).toLocaleDateString('fr-FR');
  }

  private dessinerCharts(): void {
    this.dessinerBarChart();
    this.dessinerPipelineChart();
    this.cdr.detectChanges();
  }

  private dessinerBarChart(): void {
    const canvas = this.chartCoursRef?.nativeElement;
    if (!canvas) {
      return;
    }

    const ctx = canvas.getContext('2d');
    if (!ctx) {
      return;
    }

    const width = canvas.width = canvas.offsetWidth;
    const height = canvas.height = 220;
    const data = [
      this.stats.coursPublies,
      this.stats.coursEnAttenteValidation,
      this.stats.coursBrouillons,
      this.stats.coursSupprimes,
    ];
    const labels = ['Publies', 'En attente', 'Brouillons', 'Supprimes'];
    const colors = ['#10b981', '#f59e0b', '#6366f1', '#ef4444'];
    const max = Math.max(...data, 1);

    ctx.clearRect(0, 0, width, height);

    const barWidth = 48;
    const gap = (width - barWidth * data.length) / (data.length + 1);
    const topPadding = 24;
    const bottomPadding = 36;
    const chartHeight = height - topPadding - bottomPadding;

    data.forEach((value, index) => {
      const x = gap + index * (barWidth + gap);
      const barHeight = (value / max) * chartHeight;
      const y = topPadding + chartHeight - barHeight;
      const radius = 8;
      const gradient = ctx.createLinearGradient(0, y, 0, y + Math.max(barHeight, 1));

      gradient.addColorStop(0, colors[index]);
      gradient.addColorStop(1, `${colors[index]}88`);
      ctx.fillStyle = gradient;

      ctx.beginPath();
      ctx.moveTo(x + radius, y);
      ctx.lineTo(x + barWidth - radius, y);
      ctx.quadraticCurveTo(x + barWidth, y, x + barWidth, y + radius);
      ctx.lineTo(x + barWidth, y + barHeight);
      ctx.lineTo(x, y + barHeight);
      ctx.lineTo(x, y + radius);
      ctx.quadraticCurveTo(x, y, x + radius, y);
      ctx.closePath();
      ctx.fill();

      ctx.fillStyle = '#1e1b4b';
      ctx.font = 'bold 13px Plus Jakarta Sans, sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText(String(value), x + barWidth / 2, y - 8);

      ctx.fillStyle = '#64748b';
      ctx.font = '11px Plus Jakarta Sans, sans-serif';
      ctx.fillText(labels[index], x + barWidth / 2, height - 10);
    });
  }

  private dessinerPipelineChart(): void {
    const canvas = this.chartPipelineRef?.nativeElement;
    if (!canvas) {
      return;
    }

    const ctx = canvas.getContext('2d');
    if (!ctx) {
      return;
    }

    const width = canvas.width = canvas.offsetWidth;
    const height = canvas.height = 220;
    const data = [
      this.stats.coursEnAttenteValidation,
      this.stats.formateursEnAttente,
      this.stats.paiementsEnAttente,
      this.stats.certificatsGeneres,
    ];
    const colors = ['#6366f1', '#8b5cf6', '#f59e0b', '#10b981'];
    const total = data.reduce((sum, value) => sum + value, 0) || 1;

    ctx.clearRect(0, 0, width, height);

    const centerX = width / 2;
    const centerY = height / 2;
    const outerRadius = Math.min(centerX, centerY) - 20;
    const innerRadius = outerRadius * 0.58;
    let angle = -Math.PI / 2;

    data.forEach((value, index) => {
      if (!value) {
        return;
      }

      const slice = (value / total) * Math.PI * 2;
      ctx.beginPath();
      ctx.moveTo(centerX, centerY);
      ctx.arc(centerX, centerY, outerRadius, angle, angle + slice);
      ctx.closePath();
      ctx.fillStyle = colors[index];
      ctx.fill();
      ctx.strokeStyle = '#ffffff';
      ctx.lineWidth = 2;
      ctx.stroke();
      angle += slice;
    });

    ctx.beginPath();
    ctx.arc(centerX, centerY, innerRadius, 0, Math.PI * 2);
    ctx.fillStyle = '#ffffff';
    ctx.fill();

    ctx.fillStyle = '#1e1b4b';
    ctx.font = 'bold 22px Plus Jakarta Sans, sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(String(this.pendingWorkCount), centerX, centerY - 10);
    ctx.fillStyle = '#64748b';
    ctx.font = '11px Plus Jakarta Sans, sans-serif';
    ctx.fillText('elements a traiter', centerX, centerY + 14);
  }

  private getEmptyOverview(): AdminDashboardOverview {
    return {
      totalCours: 0,
      coursPublies: 0,
      coursEnAttenteValidation: 0,
      coursBrouillons: 0,
      coursSupprimes: 0,
      totalFormateurs: 0,
      formateursActifs: 0,
      formateursEnAttente: 0,
      totalEtudiants: 0,
      etudiantsActifs: 0,
      paiementsEnAttente: 0,
      paiementsApprouves: 0,
      certificatsGeneres: 0,
      revenusPlateforme: 0,
      revenusFormateurs: 0,
    };
  }
}
