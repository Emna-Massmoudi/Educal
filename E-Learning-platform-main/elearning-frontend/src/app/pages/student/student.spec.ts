import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { CoursService } from '../../services/cours';
import { InscriptionService } from '../../services/inscription';
import { QuizService } from '../../services/quiz';
import { AvisCoursService } from '../../services/avis-cours';
import { PaiementService } from '../../services/paiement';
import { ProgressionService } from '../../services/progression';
import { CertificatService } from '../../services/certificat';
import { ToastService } from '../../services/toast.service';
import { Student } from './student';

describe('Student', () => {
  let component: Student;
  let fixture: ComponentFixture<Student>;

  beforeEach(async () => {
    TestBed.overrideComponent(Student, {
      set: { template: '' },
    });

    await TestBed.configureTestingModule({
      imports: [Student],
      providers: [
        {
          provide: CoursService,
          useValue: {
            getAllCours: () => of([]),
          },
        },
        {
          provide: InscriptionService,
          useValue: {
            getByEtudiant: () => of([]),
          },
        },
        {
          provide: QuizService,
          useValue: {},
        },
        {
          provide: AvisCoursService,
          useValue: {
            getByEtudiant: () => of([]),
          },
        },
        {
          provide: PaiementService,
          useValue: {},
        },
        {
          provide: ProgressionService,
          useValue: {
            getMyCourseProgress: () => of([]),
          },
        },
        {
          provide: CertificatService,
          useValue: {
            downloadById: () => of(),
          },
        },
        {
          provide: ToastService,
          useValue: {
            success: () => undefined,
          },
        },
        {
          provide: Router,
          useValue: { navigate: () => Promise.resolve(true) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Student);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
