package com.example.homeautomation.device;

import com.example.homeautomation.security.DeviceStateLog;
import com.example.homeautomation.security.SecureDevice;
import com.example.homeautomation.security.SecureDeviceRepository;
import com.example.homeautomation.security.SecureUser;
import com.example.homeautomation.security.SecureUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Owner-scoped device service.
 *
 *  - The current user's email ALWAYS comes from the SecurityContext (set by
 *    the JWT filter), never from a request body or URL.
 *  - Every read/update/delete filters by that email: a device that does not
 *    exist OR belongs to someone else produces the same 404.
 *  - All methods are @Transactional so LAZY associations (owner, stateLogs)
 *    can be read while building DTOs (no LazyInitializationException).
 */
@Service
public class OwnedDeviceService {

    private final SecureDeviceRepository deviceRepository;
    private final SecureUserRepository userRepository;

    public OwnedDeviceService(SecureDeviceRepository deviceRepository, SecureUserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return auth.getName();
    }

    private SecureDevice ownedOr404(Long id, String email) {
        return deviceRepository.findByIdAndOwnerEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> listMine() {
        String email = currentUserEmail();
        return deviceRepository.findAllByOwnerEmail(email).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeviceResponse getMine(Long id) {
        return toResponse(ownedOr404(id, currentUserEmail()));
    }

    @Transactional
    public DeviceResponse create(DeviceCreateRequest req) {
        String email = currentUserEmail();
        SecureUser owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String name = req.getName().trim();
        if (deviceRepository.existsByOwnerEmailAndNameIgnoreCase(email, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A device with that name already exists");
        }

        SecureDevice d = new SecureDevice();
        d.setDeviceUid("dev-" + UUID.randomUUID()); // server-generated, client cannot set it
        d.setOwner(owner);
        d.setName(name);
        d.setType(req.getType().trim());
        d.setSubtype(req.getSubtype());
        d.setRoom(req.getRoom());
        d.setState(req.getState() != null ? req.getState().name() : DeviceState.OFF.name());
        d.setValue(req.getValue() != null ? req.getValue() : 0);
        d.setRed(req.getRed() != null ? req.getRed() : 255);
        d.setGreen(req.getGreen() != null ? req.getGreen() : 255);
        d.setBlue(req.getBlue() != null ? req.getBlue() : 255);
        d.setOnline(req.isOnline());
        return toResponse(deviceRepository.save(d));
    }

    @Transactional
    public DeviceResponse updateMine(Long id, DeviceUpdateRequest req) {
        SecureDevice d = ownedOr404(id, currentUserEmail());
        if (req.getName() != null) d.setName(req.getName().trim());
        if (req.getType() != null) d.setType(req.getType().trim());
        if (req.getSubtype() != null) d.setSubtype(req.getSubtype());
        if (req.getRoom() != null) d.setRoom(req.getRoom());
        if (req.getState() != null) d.setState(req.getState().name());
        if (req.getValue() != null) d.setValue(req.getValue());
        if (req.getRed() != null) d.setRed(req.getRed());
        if (req.getGreen() != null) d.setGreen(req.getGreen());
        if (req.getBlue() != null) d.setBlue(req.getBlue());
        if (req.getOnline() != null) d.setOnline(req.getOnline());
        if (req.getActive() != null) d.setActive(req.getActive());
        return toResponse(deviceRepository.save(d));
    }

    @Transactional
    public void deleteMine(Long id) {
        SecureDevice d = ownedOr404(id, currentUserEmail());
        deviceRepository.delete(d);
    }

    @Transactional
    public DeviceResponse setState(Long id, DeviceStateRequest req) {
        String email = currentUserEmail();
        SecureDevice d = ownedOr404(id, email);

        DeviceStateLog log = newLog(d, email, req.getReason());
        log.setPreviousState(d.getState());
        log.setPreviousValue(d.getValue() != null ? d.getValue().toString() : null);

        d.setState(req.getState().name());
        if (req.getValue() != null) d.setValue(req.getValue());

        log.setNewState(d.getState());
        log.setNewValue(d.getValue() != null ? d.getValue().toString() : null);
        d.addStateLog(log); // cascaded with the device save

        return toResponse(deviceRepository.save(d));
    }

    @Transactional
    public DeviceResponse setColor(Long id, DeviceColorRequest req) {
        String email = currentUserEmail();
        SecureDevice d = ownedOr404(id, email);

        DeviceStateLog log = newLog(d, email, req.getReason());
        log.setPreviousState(d.getState());
        log.setPreviousValue(rgb(d.getRed(), d.getGreen(), d.getBlue()));

        d.setRed(req.getRed());
        d.setGreen(req.getGreen());
        d.setBlue(req.getBlue());

        log.setNewState(d.getState());
        log.setNewValue(rgb(d.getRed(), d.getGreen(), d.getBlue()));
        d.addStateLog(log);

        return toResponse(deviceRepository.save(d));
    }

    @Transactional(readOnly = true)
    public List<DeviceLogResponse> getLogs(Long id) {
        SecureDevice d = ownedOr404(id, currentUserEmail());
        return d.getStateLogs().stream()
                .sorted(Comparator.comparing(DeviceStateLog::getLogTime).reversed())
                .map(l -> new DeviceLogResponse(l.getId(), l.getPreviousState(), l.getNewState(),
                        l.getPreviousValue(), l.getNewValue(), l.getChangeReason(),
                        l.getTriggeredBy(), l.getLogTime()))
                .collect(Collectors.toList());
    }

    /** Complete log skeleton: owner (FK, NOT NULL), who triggered it, and why. */
    private DeviceStateLog newLog(SecureDevice d, String email, String reason) {
        DeviceStateLog log = new DeviceStateLog();
        log.setOwner(d.getOwner());
        log.setTriggeredBy(email);
        log.setChangeReason(reason != null && !reason.isBlank() ? reason.trim() : "USER");
        return log;
    }

    private static String rgb(Integer r, Integer g, Integer b) {
        return r + "," + g + "," + b;
    }

    private DeviceResponse toResponse(SecureDevice d) {
        DeviceResponse r = new DeviceResponse();
        r.setId(d.getId());
        r.setDeviceUid(d.getDeviceUid());
        r.setName(d.getName());
        r.setType(d.getType());
        r.setSubtype(d.getSubtype());
        r.setRoom(d.getRoom());
        r.setState(d.getState());
        r.setValue(d.getValue());
        r.setRed(d.getRed());
        r.setGreen(d.getGreen());
        r.setBlue(d.getBlue());
        r.setOnline(d.isOnline());
        r.setActive(d.isActive());
        r.setOwnerEmail(d.getOwnerEmail()); // LAZY owner — safe inside the transaction
        return r;
    }
}
