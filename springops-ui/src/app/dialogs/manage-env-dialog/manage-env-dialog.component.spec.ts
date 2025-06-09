import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageEnvDialogComponent } from './manage-env-dialog.component';

describe('ManageEnvDialogComponent', () => {
  let component: ManageEnvDialogComponent;
  let fixture: ComponentFixture<ManageEnvDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ManageEnvDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ManageEnvDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
