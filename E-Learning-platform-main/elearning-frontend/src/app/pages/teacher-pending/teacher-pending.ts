import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { PageHeroComponent } from '../../shared/ui/page-hero/page-hero';

@Component({
  standalone: true,
  selector: 'app-teacher-pending',
  imports: [CommonModule, PageHeroComponent],
  templateUrl: './teacher-pending.html',
  styleUrls: ['./teacher-pending.scss']
})
export class TeacherPending {

  constructor(private router: Router) {}

  goHome() {
    this.router.navigate(['/home']);
  }
}
