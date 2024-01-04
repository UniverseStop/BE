package be.busstop.domain.statistics.service;

import be.busstop.domain.statistics.entity.AgeStatic;
import be.busstop.domain.statistics.entity.CategoryStatic;
import be.busstop.domain.statistics.entity.GenderStatic;
import be.busstop.domain.statistics.repository.GenderStaticRepository;
import be.busstop.domain.user.entity.User;
import be.busstop.domain.user.repository.UserRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GenderStaticService {
    private final UserRepository userRepository;
    private final GenderStaticRepository genderStaticRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void setGenderStatic(){
        LocalDate date = LocalDate.now();
        List<User> userList = userRepository.findAllByCreatedAt(date.atStartOfDay());
        GenderStatic genderStatic = genderStaticRepository.findByDate(date).orElse(new GenderStatic());

        for(User user : userList){
            String gender = user.getGender();
            genderStatic.plusCnt(gender);
        }
        genderStaticRepository.save(genderStatic);

    }
}