import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../services/auth';
import { Login } from './login';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;

  beforeEach(async () => {
    TestBed.overrideComponent(Login, {
      set: { template: '' },
    });

    await TestBed.configureTestingModule({
      declarations: [Login],
      providers: [
        {
          provide: AuthService,
          useValue: {
            login: () =>
              of({
                id: 1,
                token: 'token',
                role: 'ETUDIANT',
                email: 'test@example.com',
                nom: 'Test User',
                status: 'ACTIVE',
              }),
          },
        },
        {
          provide: Router,
          useValue: { navigate: () => Promise.resolve(true) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
