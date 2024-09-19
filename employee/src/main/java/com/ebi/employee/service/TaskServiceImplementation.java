package com.ebi.employee.service;

import com.ebi.employee.entity.EmployeeEntity;
import com.ebi.employee.entity.TaskEntity;
import com.ebi.employee.exception.CustomException;
import com.ebi.employee.model.EmployeeSaveDto;
import com.ebi.employee.model.TaskDto;
import com.ebi.employee.model.TaskSaveDto;
import com.ebi.employee.repo.EmployeeRepoInterface;
import com.ebi.employee.repo.TaskRepoInterface;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@SessionAttributes("employeeEmail")
@RequiredArgsConstructor
public class TaskServiceImplementation implements TaskServiceInterface {



    private final TaskRepoInterface taskRepoInterface;
    private final ModelMapper modelMapper;
    private final EmployeeRepoInterface employeeRepoInterface;

    public List<TaskSaveDto> getAllTask (){
        List<TaskEntity> taskEntityList =taskRepoInterface.findAll();

        return taskEntityList.stream().map(Task->modelMapper
                        .map(Task,TaskSaveDto.class))
                        .collect(Collectors.toList());
    }
    public List<TaskSaveDto> getEmployeeTasks(Long employeeId){
        List<TaskEntity> taskEntityList =taskRepoInterface.findMyQuery(employeeId);
            return taskEntityList.stream().map(Task->modelMapper
                            .map(Task,TaskSaveDto.class))
                            .collect(Collectors.toList());

    }

    public TaskDto addTask(TaskSaveDto task) {
        TaskEntity taskEntity = modelMapper.map(task, TaskEntity.class);
        if (!taskEntity.getDate().matches("^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-(\\d{4})$"))
            throw new CustomException("011", "Date Form", "Please Enter Date Form dd/MM/yyyy");
        Optional <EmployeeEntity> employeeEntityOptional = employeeRepoInterface.findById(task.getEmployeeId());
        if (employeeEntityOptional.isPresent()) {
            taskEntity.setEmployeeEntity(employeeRepoInterface.getById(task.getEmployeeId()));
            taskRepoInterface.save(taskEntity);
            return modelMapper.map(taskEntity, TaskDto.class);
        }
        else
            throw new CustomException("012", "Employee Not Found", "Please Enter Employee who do this task");
    }

    @Override
    public TaskDto updateTask(TaskSaveDto task) {
        TaskEntity saveTaskEntity = null;
        if (task.getId() != null) {
            Optional<TaskEntity> taskEntityOptional = taskRepoInterface.findById(task.getId());
            if (taskEntityOptional.isPresent()) {
                if (task.getName() != null) {
                    if(!task.getName().equals(taskEntityOptional.get().getName()))
                        taskEntityOptional.get().setName(task.getName());
                    else
                        throw new CustomException("023","Name Error","Must Enter New Name ");
                }
                if (task.getDescription() != null) {
                    if(!task.getDescription().equals(taskEntityOptional.get().getDescription()))
                         taskEntityOptional.get().setDescription(task.getDescription());
                    else throw new CustomException("024","Description Error","Must Enter New Description ");
                }
                if (task.getDate() != null) {
                    if(!task.getDate().equals(taskEntityOptional.get().getDate()) && taskEntityOptional.get().getDate().matches("^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-(\\d{4})$"))
                        taskEntityOptional.get().setDate(task.getDate());
                    else throw new CustomException("025","Date Error","Must Enter New date with  Form dd/MM/yyyy");
                }
                if (task.getEmployeeId() != null) {
                    Optional<EmployeeEntity> employeeEntityOptional = employeeRepoInterface.findById(task.getEmployeeId());
                    if(!task.getEmployeeId().equals(taskEntityOptional.get().getId())&&employeeEntityOptional.isPresent())
                        taskEntityOptional.get().setEmployeeEntity(employeeEntityOptional.get());
                    else
                        throw new CustomException("026","Employee Not Found","Must Enter New Employee Id ");
                }
            }else
                throw new CustomException("022","Miss Task","No Employee with id : "+task.getId());
            saveTaskEntity = taskRepoInterface.save(taskEntityOptional.get());
        }else
            throw new CustomException ("021","Not Found Task","miss this Task data");
        return modelMapper.map(saveTaskEntity, TaskDto.class);
    }
   @Override
   public TaskDto deleteTask(@ModelAttribute("employeeEmail") String email, Long id) {
       Optional<TaskEntity> taskEntityOptional = taskRepoInterface.findById(id);

       if (taskEntityOptional.isPresent()) {
           TaskSaveDto taskSaveDto = modelMapper.map(taskEntityOptional.get(), TaskSaveDto.class);
           Optional<EmployeeEntity> employeeSaveDto = employeeRepoInterface.findById(taskSaveDto.getEmployeeId());

           if (employeeSaveDto.isPresent()) {
               EmployeeSaveDto employeeSaveDto1 = modelMapper.map(employeeSaveDto.get(), EmployeeSaveDto.class);

               if (employeeSaveDto1.getEmail().trim().equalsIgnoreCase(email.trim())) {
                   taskRepoInterface.deleteById(id);
                   return modelMapper.map(taskEntityOptional.get(), TaskDto.class);
               } else {
                   throw new CustomException("044", "Not Allowed", "Must delete only your tasks");
               }
           } else {
               throw new CustomException("031", "Not Found Employee",    "No Employee found for this task");
           }
       } else {
           throw new CustomException("031", "Not Found Task", "No Task with id : " + id + " to delete it");
       }
   }

}
