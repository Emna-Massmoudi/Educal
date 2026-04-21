import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { CategoryService } from '../../services/category';
import { CoursService } from '../../services/cours';
import { InscriptionService } from '../../services/inscription';
import { PaiementService } from '../../services/paiement';
import { Teacher } from './teacher';

describe('Teacher', () => {
  let component: Teacher;
  let fixture: ComponentFixture<Teacher>;

  beforeEach(async () => {
    TestBed.overrideComponent(Teacher, {
      set: { template: '' },
    });

    await TestBed.configureTestingModule({
      declarations: [Teacher],
      providers: [
        {
          provide: CoursService,
          useValue: {
            getCoursByFormateur: () => of([]),
          },
        },
        {
          provide: InscriptionService,
          useValue: {},
        },
        {
          provide: PaiementService,
          useValue: {
            getWalletFormateur: () => of({
              formateurId: 1,
              totalGagne: 0,
              totalCommissionPlateforme: 0,
              paiementsApprouves: 0,
            }),
          },
        },
        {
          provide: CategoryService,
          useValue: {
            getAll: () => of([]),
            getSousCategoriesByCategorieId: () => of([]),
          },
        },
        {
          provide: Router,
          useValue: { navigate: () => Promise.resolve(true) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Teacher);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
