import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { PaiementService } from '../../services/paiement';
import { AdminPaiements } from './admin-paiements';

describe('AdminPaiements', () => {
  let component: AdminPaiements;
  let fixture: ComponentFixture<AdminPaiements>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminPaiements],
      providers: [
        {
          provide: PaiementService,
          useValue: {
            getAllAdmin: () => of([]),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminPaiements);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
