import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AdminService } from '../../services/admin';
import { AdminDashboard } from './admin-dashboard';

describe('AdminDashboard', () => {
  let component: AdminDashboard;
  let fixture: ComponentFixture<AdminDashboard>;

  beforeEach(async () => {
    TestBed.overrideComponent(AdminDashboard, {
      set: { template: '' },
    });

    await TestBed.configureTestingModule({
      imports: [AdminDashboard],
      providers: [
        {
          provide: AdminService,
          useValue: {
            getDashboard: () => of({
              overview: {
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
              },
              recentCourses: [],
              topCourses: [],
              pendingFormateurs: [],
              pendingPayments: [],
            }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminDashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
