// cSpell:ignore paiements paiement approuve formateur etudiant

import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PaiementCoursResponse, PaiementService, StatutPaiement } from '../../services/paiement';

@Component({
  selector: 'app-admin-paiements',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-paiements.html',
  styleUrl: './admin-paiements.scss',
})
export class AdminPaiements implements OnInit {

  paiements: PaiementCoursResponse[] = [];
  loading = false;
  actionLoadingId: number | null = null;
  errorMessage = '';
  successMessage = '';
  filterStatut: '' | StatutPaiement = '';
  searchQuery = '';

  constructor(
    private readonly paiementService: PaiementService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.chargerPaiements();
  }

  chargerPaiements(): void {
    this.loading = true;
    this.errorMessage = '';
    this.paiementService.getAllAdmin().subscribe({
      next: (data: PaiementCoursResponse[]) => {
        this.paiements = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err: unknown) => {
        console.error(err);
        this.errorMessage = 'Impossible de charger les paiements pour le moment.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  filteredPaiements(): PaiementCoursResponse[] {
    const q = this.searchQuery.toLowerCase().trim();
    return this.paiements.filter((paiement) => {
      const matchStatus = !this.filterStatut || paiement.statut === this.filterStatut;
      const matchSearch =
        !q ||
        paiement.codePaiement.toLowerCase().includes(q) ||
        paiement.coursTitre.toLowerCase().includes(q) ||
        paiement.etudiantNom.toLowerCase().includes(q) ||
        (paiement.formateurNom ?? '').toLowerCase().includes(q);
      return matchStatus && matchSearch;
    });
  }

  approuver(paiement: PaiementCoursResponse): void {
    this.actionLoadingId = paiement.id;
    this.errorMessage = '';
    this.paiementService.approve(paiement.id).subscribe({
      next: (updated: PaiementCoursResponse) => {
        this.replacePaiement(updated);
        this.successMessage = `Paiement ${updated.codePaiement} approuve. L inscription est maintenant active.`;
        this.actionLoadingId = null;
        this.cdr.detectChanges();
      },
      error: (err: { error?: { message?: string } }) => {
        console.error(err);
        this.errorMessage = err?.error?.message ?? "Impossible d approuver ce paiement.";
        this.actionLoadingId = null;
        this.cdr.detectChanges();
      },
    });
  }

  refuser(paiement: PaiementCoursResponse): void {
    this.actionLoadingId = paiement.id;
    this.errorMessage = '';
    this.paiementService.reject(paiement.id).subscribe({
      next: (updated: PaiementCoursResponse) => {
        this.replacePaiement(updated);
        this.successMessage = `Paiement ${updated.codePaiement} refuse.`;
        this.actionLoadingId = null;
        this.cdr.detectChanges();
      },
      error: (err: { error?: { message?: string } }) => {
        console.error(err);
        this.errorMessage = err?.error?.message ?? 'Impossible de refuser ce paiement.';
        this.actionLoadingId = null;
        this.cdr.detectChanges();
      },
    });
  }

  resetFilters(): void {
    this.searchQuery = '';
    this.filterStatut = '';
  }

  get pendingCount(): number {
    return this.paiements.filter((item) => item.statut === 'EN_ATTENTE').length;
  }

  get approvedCount(): number {
    return this.paiements.filter((item) => item.statut === 'APPROUVE').length;
  }

  get rejectedCount(): number {
    return this.paiements.filter((item) => item.statut === 'REFUSE').length;
  }

  formatPrice(value: number | undefined): string {
    return `${Number(value ?? 0).toFixed(2)} TND`;
  }

  getStatutLabel(statut: StatutPaiement): string {
    const map: Record<StatutPaiement, string> = {
      EN_ATTENTE: 'En attente',
      APPROUVE: 'Approuve',
      REFUSE: 'Refuse',
    };
    return map[statut];
  }

  getStatutClass(statut: StatutPaiement): string {
    const map: Record<StatutPaiement, string> = {
      EN_ATTENTE: 'pending',
      APPROUVE: 'approved',
      REFUSE: 'rejected',
    };
    return map[statut];
  }

  private replacePaiement(updated: PaiementCoursResponse): void {
    this.paiements = this.paiements.map((item) => item.id === updated.id ? updated : item);
  }
}
