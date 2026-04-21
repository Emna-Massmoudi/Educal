import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../services/auth';
import { Register } from './register';

describe('Register', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;

  beforeEach(async () => {
    TestBed.overrideComponent(Register, {
      set: { template: '' },
    });

    await TestBed.configureTestingModule({
      declarations: [Register],
      providers: [
        {
          provide: AuthService,
          useValue: {
            register: () =>
              of({
                id: 1,
                token: 'token',
                role: 'ETUDIANT',
                email: 'test@example.com',
                nom: 'Test User',
              }),
          },
        },
        {
          provide: Router,
          useValue: { navigate: () => Promise.resolve(true) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
