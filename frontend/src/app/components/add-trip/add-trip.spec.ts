import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddTripComponent } from './add-trip.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('AddTripComponent', () => {
  let component: AddTripComponent;
  let fixture: ComponentFixture<AddTripComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddTripComponent, HttpClientTestingModule]
    }).compileComponents();

    fixture = TestBed.createComponent(AddTripComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
