import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { CategoryService } from '../../../services/category';
import { Categories } from './categories';

describe('Categories', () => {
  let component: Categories;
  let fixture: ComponentFixture<Categories>;

  beforeEach(async () => {
    TestBed.overrideComponent(Categories, {
      set: { template: '' },
    });

    await TestBed.configureTestingModule({
      imports: [Categories],
      providers: [
        {
          provide: CategoryService,
          useValue: {
            getAll: () => of([]),
            getSousCategoriesByCategorieId: () => of([]),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Categories);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
