import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InitializeAdminComponent } from './initialize-admin.component';

describe('InitializeAdminComponent', () => {
  let component: InitializeAdminComponent;
  let fixture: ComponentFixture<InitializeAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [InitializeAdminComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(InitializeAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
