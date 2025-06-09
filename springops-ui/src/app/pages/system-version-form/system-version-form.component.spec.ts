import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SystemVersionFormComponent } from './system-version-form.component';

describe('SystemVersionFormComponent', () => {
  let component: SystemVersionFormComponent;
  let fixture: ComponentFixture<SystemVersionFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SystemVersionFormComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SystemVersionFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
