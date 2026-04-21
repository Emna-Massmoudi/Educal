import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-page-hero',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './page-hero.html',
  styleUrl: './page-hero.scss',
})
export class PageHeroComponent {
  @Input() title = '';
  @Input() subtitle = '';
  @Input() eyebrow = 'EduNet';
  @Input() backgroundImage = 'assets/img/page-title/page-title.jpg';
}
