import { HttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AdminService } from '../../services/admin';
import { FormateurService } from '../../services/formateur';
import { AdminFormateurs } from './admin-formateurs';

describe('AdminFormateurs', () => {
  let component: AdminFormateurs;
  let fixture: ComponentFixture<AdminFormateurs>;

  beforeEach(async () => {
    TestBed.overrideComponent(AdminFormateurs, {
      set: { template: '' },
    });

    await TestBed.configureTestingModule({
      imports: [AdminFormateurs],
      providers: [
        {
          provide: FormateurService,
          useValue: {
            getFormateursEnAttente: () => of([]),
          },
        },
        {
          provide: AdminService,
          useValue: {},
        },
        {
          provide: HttpClient,
          useValue: {
            get: () => of([]),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminFormateurs);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
