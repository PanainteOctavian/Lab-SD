package com.sd.laborator.business.services

import com.sd.laborator.business.helpers.AesHelper
import com.sd.laborator.business.helpers.PasswordHelper
import com.sd.laborator.business.interfaces.IMemberService
import com.sd.laborator.business.models.*
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class MemberService : IMemberService {

    private val members = ConcurrentHashMap<Int, FamilyMember>()
    private val idCounter = AtomicInteger(1)

    override fun register(req: RegisterRequest): MyCustomResponse<Any> {
        return try {
            if (req.username.isBlank() || req.password.isBlank() ||
                req.firstName.isBlank() || req.lastName.isBlank()
            ) {
                return MyCustomResponse(
                    successfulOperation = false,
                    code = 400,
                    data = Unit,
                    error = "Bad Request",
                    message = "Toate câmpurile sunt obligatorii."
                )
            }

            val existingUser = members.values.any { it.username == req.username }
            if (existingUser) {
                return MyCustomResponse(
                    successfulOperation = false,
                    code = 409,
                    data = Unit,
                    error = "Conflict",
                    message = "Username-ul '${req.username}' este deja folosit."
                )
            }

            val id = idCounter.getAndIncrement()
            val member = FamilyMember(
                id = id,
                username = req.username,
                passwordHash = PasswordHelper.hash(req.username, req.password),
                encryptedFirstName = AesHelper.encrypt(req.firstName),   // GDPR
                encryptedLastName  = AesHelper.encrypt(req.lastName)     // GDPR
            )
            members[id] = member

            MyCustomResponse(
                successfulOperation = true,
                code = 201,
                data = mapOf("id" to id, "username" to req.username)
            )
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    override fun login(req: LoginRequest): MyCustomResponse<Any> {
        return try {
            val member = members.values.find { it.username == req.username }
                ?: return MyCustomResponse(
                    successfulOperation = false, code = 401, data = Unit,
                    error = "Unauthorized", message = "Credențiale incorecte."
                )

            if (!PasswordHelper.matches(req.username, req.password, member.passwordHash)) {
                return MyCustomResponse(
                    successfulOperation = false, code = 401, data = Unit,
                    error = "Unauthorized", message = "Credențiale incorecte."
                )
            }

            MyCustomResponse(
                successfulOperation = true,
                code = 200,
                data = mapOf("id" to member.id, "username" to member.username)
            )
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    override fun getMember(id: Int): MyCustomResponse<Any> {
        return try {
            val member = members[id]
                ?: return MyCustomResponse(
                    successfulOperation = false, code = 404, data = Unit,
                    error = "Not Found", message = "Membrul cu id=$id nu există."
                )

            MyCustomResponse(
                successfulOperation = true,
                code = 200,
                data = MemberResponse(
                    id = member.id,
                    username = member.username,
                    firstName = AesHelper.decrypt(member.encryptedFirstName),
                    lastName  = AesHelper.decrypt(member.encryptedLastName)
                )
            )
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    override fun getAllMembers(): MyCustomResponse<Any> {
        return try {
            val list = members.values.map { m ->
                MemberResponse(
                    id = m.id,
                    username = m.username,
                    firstName = AesHelper.decrypt(m.encryptedFirstName),
                    lastName  = AesHelper.decrypt(m.encryptedLastName)
                )
            }
            if (list.isEmpty())
                MyCustomResponse(successfulOperation = true, code = 204, data = emptyList<MemberResponse>())
            else
                MyCustomResponse(successfulOperation = true, code = 200, data = list)
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    override fun deleteMember(id: Int): MyCustomResponse<Any> {
        return try {
            if (!members.containsKey(id))
                return MyCustomResponse(
                    successfulOperation = false, code = 404, data = Unit,
                    error = "Not Found", message = "Membrul cu id=$id nu există."
                )
            members.remove(id)
            MyCustomResponse(successfulOperation = true, code = 200, data = Unit)
        } catch (e: Exception) {
            MyCustomResponse(successfulOperation = false, code = 500, data = Unit, error = e.message)
        }
    }

    fun memberExists(id: Int) = members.containsKey(id)
}