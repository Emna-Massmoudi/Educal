import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CategoryService, Categorie } from '../../services/category';
import { PageHeroComponent } from '../../shared/ui/page-hero/page-hero';

@Component({
  selector: 'app-student-onboarding',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, PageHeroComponent],
  templateUrl: './student-onboarding.html',
  styleUrl: './student-onboarding.scss',
})
export class StudentOnboardingComponent implements OnInit {
  categories: Categorie[] = [];
  selectedInterests: string[] = [];
  loading = false;
  saving = false;

  constructor(
    private readonly categoryService: CategoryService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.selectedInterests = this.getStoredInterests();
    this.loading = true;

    this.categoryService.getAll().subscribe({
      next: (categories) => {
        this.categories = categories;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  toggleInterest(interest: string): void {
    if (this.selectedInterests.includes(interest)) {
      this.selectedInterests = this.selectedInterests.filter((item) => item !== interest);
      return;
    }

    this.selectedInterests = [...this.selectedInterests, interest];
  }

  isSelected(interest: string): boolean {
    return this.selectedInterests.includes(interest);
  }

  savePreferences(): void {
    this.saving = true;
    const userId = localStorage.getItem('userId') ?? 'anonymous';
    localStorage.setItem(`student_interests_${userId}`, JSON.stringify(this.selectedInterests));
    localStorage.setItem('student_onboarding_completed', 'true');
    this.router.navigate(['/home']);
  }

  skip(): void {
    this.savePreferences();
  }

  private getStoredInterests(): string[] {
    const userId = localStorage.getItem('userId') ?? 'anonymous';

    try {
      const raw = localStorage.getItem(`student_interests_${userId}`);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }
}
