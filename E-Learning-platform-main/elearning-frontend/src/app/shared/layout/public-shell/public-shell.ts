import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FooterComponent } from '../educal-footer/educal-footer';
import { EducalHeaderComponent } from '../educal-header/educal-header';

@Component({
  selector: 'app-public-shell',
  standalone: true,
  imports: [RouterOutlet, EducalHeaderComponent, FooterComponent ],
  templateUrl: './public-shell.html',
  styleUrl: './public-shell.scss',
})
export class PublicShellComponent {}
