import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { FormateurService } from '../../services/formateur';
import { TeacherProfile } from './teacher-profile';

describe('TeacherProfile', () => {
  let component: TeacherProfile;
  let fixture: ComponentFixture<TeacherProfile>;

  beforeEach(async () => {
    TestBed.overrideComponent(TeacherProfile, {
      set: { template: '' },
    });

    await TestBed.configureTestingModule({
      declarations: [TeacherProfile],
      providers: [
        {
          provide: FormateurService,
          useValue: {
            getFormateurById: () =>
              of({
                id: 1,
                nom: 'Test Formateur',
                email: 'formateur@example.com',
                status: 'ACTIVE',
                portfolio: '',
                specialite: '',
                bio: '',
                cvUrl: '',
                diplomeUrl: '',
                certificatUrl: '',
                attestationUrl: '',
                motivation: '',
                commentaireAdmin: '',
              }),
          },
        },
        {
          provide: Router,
          useValue: { navigate: () => Promise.resolve(true) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TeacherProfile);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
