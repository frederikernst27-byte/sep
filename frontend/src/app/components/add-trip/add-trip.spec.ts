import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddTripComponent } from './add-trip.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

describe('AddTripComponent', () => {
  let component: AddTripComponent;
  let fixture: ComponentFixture<AddTripComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddTripComponent, HttpClientTestingModule, RouterTestingModule]
    }).compileComponents();

    fixture = TestBed.createComponent(AddTripComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('isValid should be false when fields empty', () => {
    expect(component.isValid).toBeFalse();
  });

  it('isValid should be true when all fields filled correctly', () => {
    component.name = 'Urlaub';
    component.destination = 'Barcelona';
    component.startDate = '2026-06-01';
    component.endDate = '2026-06-10';
    expect(component.isValid).toBeTrue();
  });

  it('isValid should be false when endDate before startDate', () => {
    component.name = 'Urlaub';
    component.destination = 'Barcelona';
    component.startDate = '2026-06-10';
    component.endDate = '2026-06-01';
    expect(component.isValid).toBeFalse();
  });

  it('should fill fields after file extraction', () => {
    const mockFile = new File(['dummy'], 'test.pdf', { type: 'application/pdf' });
    const event = { target: { files: [mockFile] } };

    component.onFileSelected(event);

    const req = httpMock.expectOne('http://localhost:8080/api/extract');
    expect(req.request.method).toBe('POST');
    req.flush({
      name: 'Testreise',
      destination: 'Berlin',
      startDate: '2026-07-01',
      endDate: '2026-07-05'
    });

    expect(component.name).toBe('Testreise');
    expect(component.destination).toBe('Berlin');
  });

  it('should show error on failed extraction', () => {
    const mockFile = new File(['dummy'], 'test.pdf', { type: 'application/pdf' });
    component.onFileSelected({ target: { files: [mockFile] } });

    const req = httpMock.expectOne('http://localhost:8080/api/extract');
    req.error(new ErrorEvent('Network error'));

    expect(component.errorMsg).toBeTruthy();
    expect(component.extracting).toBeFalse();
  });
});
