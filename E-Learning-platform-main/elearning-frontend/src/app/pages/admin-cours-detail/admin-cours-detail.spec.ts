import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { CoursService } from '../../services/cours';
import { AdminCoursDetail } from './admin-cours-detail';

describe('AdminCoursDetail', () => {
  let component: AdminCoursDetail;
  let fixture: ComponentFixture<AdminCoursDetail>;

  beforeEach(async () => {
    TestBed.overrideComponent(AdminCoursDetail, {
      set: { template: '' },
    });

    await TestBed.configureTestingModule({
      imports: [AdminCoursDetail],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ id: '1' }),
            },
          },
        },
        {
          provide: Router,
          useValue: { navigate: () => Promise.resolve(true) },
        },
        {
          provide: CoursService,
          useValue: {
            getCoursById: () =>
              of({
                id: 1,
                titre: 'Cours test',
                description: 'Description',
                etatPublication: 'BROUILLON',
                statut: 'BROUILLON',
                categorieId: 1,
                categorieNom: 'Categorie',
                sousCategorieId: 1,
                sousCategorieNom: 'Sous-categorie',
                formateurId: 1,
                formateurNom: 'Formateur',
              }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminCoursDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
